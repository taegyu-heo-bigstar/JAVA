import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/*
 * @breife: AccountManager 클래스는 계좌 생성을 비롯해 입금, 출금, 이체시의 원자성 유지 기능을 담당하며, 파일 입출력과 연동하여 계좌 정보를 관리합니다.
 *          Account 클래스는 AccountManager의 nested class로 포함됩니다.
 *
 * @note: 모든 파일 입출력은 "account_info.csv" 파일을 사용합니다.
 */
public class AccountManager {

    public static class Account {
        private final String code;
        private final String own;
        private final String pw;
        private int bal;

        Account(String code, String own, int bal, String pw) {
            this.code = code;
            this.own = own;
            this.bal = bal;
            this.pw = pw;
        }

        boolean deposit(int amnt) {
            if (amnt <= 0) return false;
            bal += amnt;
            return true;
        }

        boolean withdraw(int amnt, String pw) {
            if (amnt <= 0) return false;
            if (!this.pw.equals(pw)) return false;
            if (this.bal < amnt) return false;
            bal -= amnt;
            return true;
        }

        boolean transfer(Account acc, int amnt, String pw) {
            if (acc == null) return false;
            if (amnt <= 0) return false;
            if (!withdraw(amnt, pw)) return false;
            return acc.deposit(amnt);
        }

        boolean show(String pw) {
            if (!this.pw.equals(pw)) return false;
            System.out.println(own + "님의 잔액 : " + bal);
            return true;
        }

        boolean verifyPassword(String pw) {
            return this.pw.equals(pw);
        }

        String getOwner() {
            return own;
        }

        String getCode() {
            return code;
        }

        int getBal() {
            return bal;
        }

        // CSV 저장(한 줄) 포맷: code,owner,balance,password
        @Override
        public String toString() {
            return csvField(code) + "," + csvField(own) + "," + bal + "," + csvField(pw);
        }

        static Account fromString(String line) {
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 4) {
                throw new IllegalArgumentException("Invalid account record: " + line);
            }
            String code = fields.get(0);
            String owner = fields.get(1);
            int balance = Integer.parseInt(fields.get(2));
            String password = fields.get(3);
            return new Account(code, owner, balance, password);
        }

        private static String csvField(String value) {
            if (value == null) return "";
            boolean needsQuoting = value.indexOf(',') >= 0
                    || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0
                    || value.indexOf('\r') >= 0;
            String escaped = value.replace("\"", "\"\"");
            return needsQuoting ? ("\"" + escaped + "\"") : escaped;
        }

        private static List<String> parseCsvLine(String line) {
            List<String> out = new ArrayList<>();
            if (line == null) return out;
            StringBuilder cur = new StringBuilder();
            boolean inQuotes = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (inQuotes) {
                    if (c == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            cur.append('"');
                            i++;
                        } else {
                            inQuotes = false;
                        }
                    } else {
                        cur.append(c);
                    }
                } else {
                    if (c == ',') {
                        out.add(cur.toString());
                        cur.setLength(0);
                    } else if (c == '"') {
                        inQuotes = true;
                    } else {
                        cur.append(c);
                    }
                }
            }
            out.add(cur.toString());
            return out;
        }
    }

    private static final Path ACCOUNT_FILE = Paths.get("account_info.csv");

    private static Map<String, Account> deepCopyAccounts(Map<String, Account> accounts) {
        Map<String, Account> copy = new LinkedHashMap<>();
        if (accounts == null) return copy;
        for (Account acc : accounts.values()) {
            if (acc == null) continue;
            Account cloned = new Account(acc.code, acc.own, acc.bal, acc.pw);
            copy.put(cloned.getCode(), cloned);
        }
        return copy;
    }

    private static void commitAccounts(Map<String, Account> target, Map<String, Account> source) {
        if (target == null) return;
        target.clear();
        if (source != null) target.putAll(source);
    }

    public static Map<String, Account> loadAllAccounts() {
        Map<String, Account> accounts = new LinkedHashMap<>();
        if (!Files.exists(ACCOUNT_FILE)) {
            return accounts;
        }
        try {
            List<String> lines = Files.readAllLines(ACCOUNT_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null) continue;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                Account acc = Account.fromString(trimmed);
                accounts.put(acc.getCode(), acc);
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("❌ 파일 읽기 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return accounts;
    }

    public static boolean saveAllAccounts(Map<String, Account> accounts) {
        Path parent = ACCOUNT_FILE.getParent();
        try {
            if (parent != null) Files.createDirectories(parent);
            Path tmp = ACCOUNT_FILE.resolveSibling(ACCOUNT_FILE.getFileName().toString() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                for (Account acc : accounts.values()) {
                    writer.write(acc.toString());
                    writer.newLine();
                }
            }
            Files.move(tmp, ACCOUNT_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
            e.printStackTrace();
            return false;
        }
    }

    public static Account createAccount(Scanner scanner, Map<String, Account> accounts) {
        System.out.println("=== 계좌 생성===");

        String accNum;
        while (true) {
            accNum = getValidInput(
                    scanner,
                    "계좌번호를 입력하세요 (형식: 1234-1234): ",
                    "^\\d{4}-\\d{4}$",
                    "잘못된 형식입니다. '1234-1234' 형태로 입력해주세요."
            );
            if (accounts.containsKey(accNum)) {
                System.out.println("이미 존재하는 계좌번호입니다. 다른 번호를 입력해주세요.");
                continue;
            }
            break;
        }

        String owner = getValidInput(
                scanner,
                "소유자 이름을 입력하세요 (한글 또는 영문): ",
                "^[가-힣a-zA-Z]+$",
                "이름은 한글 또는 영문만 가능합니다."
        );

        String balanceStr = getValidInput(
                scanner,
                "잔액을 입력하세요 (숫자만): ",
                "^[0-9]+$",
                "0 이상의 숫자만 입력해주세요."
        );
        int balance = Integer.parseInt(balanceStr);

        String password = getValidInput(
                scanner,
                "비밀번호를 입력하세요 (4자리 이상): ",
                "^.{4,}$",
                "비밀번호는 최소 4자리 이상이어야 합니다."
        );

        Map<String, Account> snapshot = deepCopyAccounts(accounts);
        Account myAccount = new Account(accNum, owner, balance, password);
        snapshot.put(myAccount.getCode(), myAccount);

        if (!saveAllAccounts(snapshot)) {
            System.out.println("계좌 생성에 실패했습니다.");
            return null;
        }

        commitAccounts(accounts, snapshot);
        System.out.println("✅ 계좌가 성공적으로 생성되었습니다.");
        return accounts.get(accNum);
    }

    public static boolean createAccount(Map<String, Account> accounts, String accNum, String owner, int balance, String password) {
        if (accounts == null) return false;
        if (accNum == null || owner == null || password == null) return false;
        if (accounts.containsKey(accNum)) return false;
        if (balance < 0) return false;
        if (password.length() < 4) return false;

        Map<String, Account> snapshot = deepCopyAccounts(accounts);
        snapshot.put(accNum, new Account(accNum, owner, balance, password));
        if (!saveAllAccounts(snapshot)) return false;
        commitAccounts(accounts, snapshot);
        return true;
    }

    public static boolean deposit(Map<String, Account> accounts, Account account, int amount) {
        if (accounts == null || account == null) {
            System.out.println("❌ 입금에 실패했습니다.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("❌ 입금에 실패했습니다.");
            return false;
        }

        Map<String, Account> snapshot = deepCopyAccounts(accounts);
        Account snapAcc = snapshot.get(account.getCode());
        if (snapAcc == null || !snapAcc.deposit(amount)) {
            System.out.println("❌ 입금에 실패했습니다.");
            return false;
        }
        if (!saveAllAccounts(snapshot)) {
            System.out.println("❌ 입금에 실패했습니다.");
            return false;
        }
        commitAccounts(accounts, snapshot);
        System.out.println("✅ 입금이 완료되었습니다.");
        return true;
    }

    public static boolean withdraw(Map<String, Account> accounts, Account account, int amount, String password) {
        if (accounts == null || account == null) {
            System.out.println("❌ 출금에 실패했습니다.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("❌ 출금에 실패했습니다.");
            return false;
        }

        Map<String, Account> snapshot = deepCopyAccounts(accounts);
        Account snapAcc = snapshot.get(account.getCode());
        if (snapAcc == null || !snapAcc.withdraw(amount, password)) {
            System.out.println("❌ 출금에 실패했습니다.");
            return false;
        }
        if (!saveAllAccounts(snapshot)) {
            System.out.println("❌ 출금에 실패했습니다.");
            return false;
        }
        commitAccounts(accounts, snapshot);
        System.out.println("✅ 출금이 완료되었습니다.");
        return true;
    }

    public static boolean transfer(Map<String, Account> accounts, Account fromAccount, Account toAccount, int amount, String password) {
        if (accounts == null || fromAccount == null || toAccount == null) {
            System.out.println("❌ 이체에 실패했습니다.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("❌ 이체에 실패했습니다.");
            return false;
        }

        Map<String, Account> snapshot = deepCopyAccounts(accounts);
        Account fromSnap = snapshot.get(fromAccount.getCode());
        Account toSnap = snapshot.get(toAccount.getCode());
        if (fromSnap == null || toSnap == null || !fromSnap.transfer(toSnap, amount, password)) {
            System.out.println("❌ 이체에 실패했습니다.");
            return false;
        }
        if (!saveAllAccounts(snapshot)) {
            System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
            return false;
        }
        commitAccounts(accounts, snapshot);
        System.out.println("송금에 성공하였습니다.");
        return true;
    }

    private static String getValidInput(Scanner scanner, String prompt, String regex, String errorMsg) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine();
            if (Pattern.matches(regex, input)) {
                break;
            } else {
                System.out.println(errorMsg);
            }
        }
        return input;
    }
}
