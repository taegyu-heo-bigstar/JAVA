import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

public class Main {

    private static final String ACC_REGEX = "^\\d{4}-\\d{4}$";

    public static void main(String[] args) {
        Map<String, AccountManager.Account> accounts = AccountManager.loadAllAccounts();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("==== 계좌 관리 ====");
                System.out.println("1) 계좌 생성");
                System.out.println("2) 입금");
                System.out.println("3) 출금");
                System.out.println("4) 이체");
                System.out.println("5) 잔액 조회");
                System.out.println("9) GUI 실행(Swing)");
                System.out.println("0) 종료");
                System.out.print("선택: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        AccountManager.createAccount(scanner, accounts);
                        break;
                    case "2":
                        handleDeposit(scanner, accounts);
                        break;
                    case "3":
                        handleWithdraw(scanner, accounts);
                        break;
                    case "4":
                        handleTransfer(scanner, accounts);
                        break;
                    case "5":
                        handleBalance(scanner, accounts);
                        break;
                    case "9":
                        launchGui(accounts);
                        break;
                    case "0":
                        System.out.println("프로그램을 종료합니다.");
                        return;
                    default:
                        System.out.println("올바른 메뉴 번호를 입력하세요.");
                }
            }
        }
    }

    private static void handleDeposit(Scanner scanner, Map<String, AccountManager.Account> accounts) {
        String code = readAccountCode(scanner, "입금할 계좌번호(1234-1234): ");
        AccountManager.Account acc = accounts.get(code);
        if (acc == null) {
            System.out.println("❌ 존재하지 않는 계좌번호입니다.");
            return;
        }
        int amount = readPositiveInt(scanner, "입금액(1 이상): ");
        AccountManager.deposit(accounts, acc, amount);
    }

    private static void handleWithdraw(Scanner scanner, Map<String, AccountManager.Account> accounts) {
        String code = readAccountCode(scanner, "출금할 계좌번호(1234-1234): ");
        AccountManager.Account acc = accounts.get(code);
        if (acc == null) {
            System.out.println("❌ 존재하지 않는 계좌번호입니다.");
            return;
        }
        int amount = readPositiveInt(scanner, "출금액(1 이상): ");
        System.out.print("비밀번호: ");
        String pw = scanner.nextLine();
        AccountManager.withdraw(accounts, acc, amount, pw);
    }

    private static void handleTransfer(Scanner scanner, Map<String, AccountManager.Account> accounts) {
        String fromCode = readAccountCode(scanner, "출금 계좌번호(1234-1234): ");
        AccountManager.Account fromAcc = accounts.get(fromCode);
        if (fromAcc == null) {
            System.out.println("❌ 출금 계좌번호가 존재하지 않습니다.");
            return;
        }

        String toCode = readAccountCode(scanner, "입금 계좌번호(1234-1234): ");
        AccountManager.Account toAcc = accounts.get(toCode);
        if (toAcc == null) {
            System.out.println("❌ 입금 계좌번호가 존재하지 않습니다.");
            return;
        }
        if (fromAcc == toAcc) {
            System.out.println("❌ 동일 계좌로는 이체할 수 없습니다.");
            return;
        }

        int amount = readPositiveInt(scanner, "이체액(1 이상): ");
        System.out.print("출금 계좌 비밀번호: ");
        String pw = scanner.nextLine();
        AccountManager.transfer(accounts, fromAcc, toAcc, amount, pw);
    }

    private static void handleBalance(Scanner scanner, Map<String, AccountManager.Account> accounts) {
        String code = readAccountCode(scanner, "조회할 계좌번호(1234-1234): ");
        AccountManager.Account acc = accounts.get(code);
        if (acc == null) {
            System.out.println("❌ 존재하지 않는 계좌번호입니다.");
            return;
        }
        System.out.print("비밀번호: ");
        String pw = scanner.nextLine();
        if (!acc.verifyPassword(pw)) {
            System.out.println("❌ 비밀번호가 올바르지 않습니다.");
            return;
        }
        System.out.println(acc.getOwner() + "님의 잔액 : " + acc.getBal());
    }

    private static void launchGui(Map<String, AccountManager.Account> accounts) {
        SwingUtilities.invokeLater(() -> new UserInterface(accounts).open());
    }

    private static String readAccountCode(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (Pattern.matches(ACC_REGEX, input)) {
                return input;
            }
            System.out.println("❌ 계좌번호 형식이 올바르지 않습니다. 예: 1234-1234");
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(text);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {
                // fall through
            }
            System.out.println("❌ 1 이상의 숫자를 입력하세요.");
        }
    }
}
