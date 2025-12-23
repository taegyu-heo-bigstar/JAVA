package Report3;

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
import java.util.concurrent.locks.ReentrantLock;

/*
 * @breife: AccountManager 클래스는 계좌 생성을 비롯해 입금, 출금, 이체시의 원자성 유지 기능을 담당하며, 파일 입출력과 연동하여 계좌 정보를 관리합니다.
 *          Account 클래스는 AccountManager의 nested class로 포함됩니다.
 *
 * @note: 모든 파일 입출력은 "account_info.csv" 파일을 사용합니다.
 */
public class AccountManager {

    private static final ReentrantLock FILE_IO_LOCK = new ReentrantLock();

    public static final class Result {
        public final boolean ok;
        public final String message;
        public final Integer balance;

        private Result(boolean ok, String message, Integer balance) {
            this.ok = ok;
            this.message = message;
            this.balance = balance;
        }

        public static Result ok(String message) {
            return new Result(true, message, null);
        }

        public static Result okWithBalance(String message, int balance) {
            return new Result(true, message, balance);
        }

        public static Result fail(String message) {
            return new Result(false, message, null);
        }
    }


    public static class Account {
        private final String code;
        private final String own;
        private final String pwRecord;
        private int bal;

        Account(String code, String own, int bal, String pwRecord) {
            this.code = code;
            this.own = own;
            this.bal = bal;
            this.pwRecord = pwRecord;
        }

        boolean deposit(int amnt) {
            if (amnt <= 0) return false;
            bal += amnt;
            return true;
        }

        boolean withdraw(int amnt, String pw) {
            if (amnt <= 0) return false;
            if (!verifyPassword(pw)) return false;
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
            return verifyPassword(pw);
        }

        boolean verifyPassword(String pw) {
            return PasswordHasher.verify(pw, this.pwRecord);
        }

        boolean hasLegacyPasswordRecord() {
            return PasswordHasher.isLegacyPlaintextRecord(this.pwRecord);
        }

        Account withHashedPasswordRecord(String plainPassword) {
            if (plainPassword == null) return this;
            if (!verifyPassword(plainPassword)) return this;
            if (!hasLegacyPasswordRecord()) return this;
            return new Account(this.code, this.own, this.bal, PasswordHasher.hash(plainPassword));
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
            return csvField(code) + "," + csvField(own) + "," + bal + "," + csvField(pwRecord);
        }

        static Account fromString(String line) {
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 4) {
                throw new IllegalArgumentException("Invalid account record: " + line);
            }
            String code = fields.get(0);
            String owner = fields.get(1);
            int balance = Integer.parseInt(fields.get(2));
            String pwRecord = fields.get(3);
            return new Account(code, owner, balance, pwRecord);
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

    // ===== GUI 전용: 검증/업무처리를 여기서 수행하고 결과 메시지를 반환 =====

    public static Result createAccountFromUi(
            Map<String, Account> accounts,
            String accNum,
            String owner,
            String balanceText,
            String password
    ) {
        try {
            ErrorManagement.requireAccountsReady(accounts);

            String acc = ErrorManagement.trimToEmpty(accNum);
            String own = ErrorManagement.trimToEmpty(owner);
            ErrorManagement.validateAccountCode(acc);
            ErrorManagement.requireAccountNotExists(accounts, acc);
            ErrorManagement.validateOwner(own);
            int balance = ErrorManagement.parseNonNegativeInt("잔액", balanceText);
            ErrorManagement.validatePasswordMinLength(password);

            boolean ok = createAccount(accounts, acc, own, balance, password);
            return ok ? Result.ok("계좌가 성공적으로 생성되었습니다.") : Result.fail("계좌 생성에 실패했습니다.");
        } catch (ErrorManagement.ValidationException e) {
            return Result.fail(e.getMessage());
        }
    }

    public static Result depositFromUi(Map<String, Account> accounts, String accNum, String amountText) {
        try {
            ErrorManagement.requireAccountsReady(accounts);
            String acc = ErrorManagement.trimToEmpty(accNum);
            ErrorManagement.validateAccountCode(acc);
            int amount = ErrorManagement.parsePositiveInt("입금액", amountText);
            Account account = ErrorManagement.requireAccountExists(accounts, acc);

            boolean ok = deposit(accounts, account, amount);
            return ok ? Result.ok("입금이 완료되었습니다.") : Result.fail("입금에 실패했습니다.");
        } catch (ErrorManagement.ValidationException e) {
            return Result.fail(e.getMessage());
        }
    }

    public static Result withdrawFromUi(Map<String, Account> accounts, String accNum, String amountText, String password) {
        try {
            ErrorManagement.requireAccountsReady(accounts);
            String acc = ErrorManagement.trimToEmpty(accNum);
            ErrorManagement.validateAccountCode(acc);
            int amount = ErrorManagement.parsePositiveInt("출금액", amountText);
            ErrorManagement.requirePasswordProvided(password);
            Account account = ErrorManagement.requireAccountExists(accounts, acc);
            ErrorManagement.requirePasswordMatches(account, password);
            ErrorManagement.requireSufficientBalance(account, amount);

            boolean ok = withdraw(accounts, account, amount, password);
            return ok ? Result.ok("출금이 완료되었습니다.") : Result.fail("출금에 실패했습니다.");
        } catch (ErrorManagement.ValidationException e) {
            return Result.fail(e.getMessage());
        }
    }

    public static Result transferFromUi(
            Map<String, Account> accounts,
            String fromAccNum,
            String toAccNum,
            String amountText,
            String password
    ) {
        try {
            ErrorManagement.requireAccountsReady(accounts);
            String from = ErrorManagement.trimToEmpty(fromAccNum);
            String to = ErrorManagement.trimToEmpty(toAccNum);
            ErrorManagement.validateAccountCode(from);
            ErrorManagement.validateAccountCode(to);
            ErrorManagement.requireDifferentAccounts(from, to);
            int amount = ErrorManagement.parsePositiveInt("이체액", amountText);
            ErrorManagement.requirePasswordProvided(password);

            Account fromAcc = ErrorManagement.requireAccountExists(accounts, from);
            Account toAcc = ErrorManagement.requireAccountExists(accounts, to);
            ErrorManagement.requirePasswordMatches(fromAcc, password);
            ErrorManagement.requireSufficientBalance(fromAcc, amount);

            boolean ok = transfer(accounts, fromAcc, toAcc, amount, password);
            return ok ? Result.ok("이체에 성공하였습니다.") : Result.fail("이체에 실패했습니다.");
        } catch (ErrorManagement.ValidationException e) {
            return Result.fail(e.getMessage());
        }
    }

    public static Result balanceFromUi(Map<String, Account> accounts, String accNum, String password) {
        try {
            ErrorManagement.requireAccountsReady(accounts);
            String acc = ErrorManagement.trimToEmpty(accNum);
            ErrorManagement.validateAccountCode(acc);
            ErrorManagement.requirePasswordProvided(password);
            Account account = ErrorManagement.requireAccountExists(accounts, acc);
            ErrorManagement.requirePasswordMatches(account, password);
            return Result.okWithBalance(account.getOwner() + "님의 잔액: " + account.getBal(), account.getBal());
        } catch (ErrorManagement.ValidationException e) {
            return Result.fail(e.getMessage());
        }
    }

    private static Map<String, Account> deepCopyAccounts(Map<String, Account> accounts) {
        Map<String, Account> copy = new LinkedHashMap<>();
        if (accounts == null) return copy;
        for (Account acc : accounts.values()) {
            if (acc == null) continue;
            Account cloned = new Account(acc.code, acc.own, acc.bal, acc.pwRecord);
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
        FILE_IO_LOCK.lock();
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
            // GUI 전용 모드: 호출 측에서 필요 시 처리(여기서는 조용히 빈 목록 반환)
        } finally {
            FILE_IO_LOCK.unlock();
        }
        return accounts;
    }

    public static boolean saveAllAccounts(Map<String, Account> accounts) {
        Path parent = ACCOUNT_FILE.getParent();
        FILE_IO_LOCK.lock();
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
            return false;
        } finally {
            FILE_IO_LOCK.unlock();
        }
    }

    public static boolean createAccount(Map<String, Account> accounts, String accNum, String owner, int balance, String password) {
        if (accounts == null) return false;
        if (accNum == null || owner == null || password == null) return false;
        if (accounts.containsKey(accNum)) return false;
        if (balance < 0) return false;
        if (password.length() < 4) return false;

        synchronized (accounts) {
            Map<String, Account> snapshot = deepCopyAccounts(accounts);
            snapshot.put(accNum, new Account(accNum, owner, balance, PasswordHasher.hash(password)));
            if (!saveAllAccounts(snapshot)) return false;
            commitAccounts(accounts, snapshot);
            return true;
        }
    }

    public static boolean deposit(Map<String, Account> accounts, Account account, int amount) {
        if (accounts == null || account == null) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }

        synchronized (accounts) {
            Map<String, Account> snapshot = deepCopyAccounts(accounts);
            Account snapAcc = snapshot.get(account.getCode());
            if (snapAcc == null || !snapAcc.deposit(amount)) {
                return false;
            }
            if (!saveAllAccounts(snapshot)) {
                return false;
            }
            commitAccounts(accounts, snapshot);
            return true;
        }
    }

    public static boolean withdraw(Map<String, Account> accounts, Account account, int amount, String password) {
        if (accounts == null || account == null) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }

        synchronized (accounts) {
            Map<String, Account> snapshot = deepCopyAccounts(accounts);
            Account snapAcc = snapshot.get(account.getCode());
            if (snapAcc == null) {
                return false;
            }
            if (snapAcc.hasLegacyPasswordRecord() && snapAcc.verifyPassword(password)) {
                snapAcc = snapAcc.withHashedPasswordRecord(password);
                snapshot.put(snapAcc.getCode(), snapAcc);
            }
            if (!snapAcc.withdraw(amount, password)) {
                return false;
            }
            if (!saveAllAccounts(snapshot)) {
                return false;
            }
            commitAccounts(accounts, snapshot);
            return true;
        }
    }

    public static boolean transfer(Map<String, Account> accounts, Account fromAccount, Account toAccount, int amount, String password) {
        if (accounts == null || fromAccount == null || toAccount == null) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }

        synchronized (accounts) {
            Map<String, Account> snapshot = deepCopyAccounts(accounts);
            Account fromSnap = snapshot.get(fromAccount.getCode());
            Account toSnap = snapshot.get(toAccount.getCode());
            if (fromSnap == null || toSnap == null) {
                return false;
            }
            if (fromSnap.hasLegacyPasswordRecord() && fromSnap.verifyPassword(password)) {
                fromSnap = fromSnap.withHashedPasswordRecord(password);
                snapshot.put(fromSnap.getCode(), fromSnap);
            }
            if (!fromSnap.transfer(toSnap, amount, password)) {
                return false;
            }
            if (!saveAllAccounts(snapshot)) {
                return false;
            }
            commitAccounts(accounts, snapshot);
            return true;
        }
    }
}
