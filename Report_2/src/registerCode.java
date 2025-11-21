import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

// 1. Account 객체 정의
class Account {
    private String accountNumber; // 예: 1234-1234
    private String owner;         // 예: 홍길동
    private int balance;          // 예: 1000
    private String password;      // 예: qwer

    // 생성자
    public Account(String accountNumber, String owner, int balance, String password) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
        this.password = password;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class AccountManager {
    Account createAccount(Scanner scanner) {
        System.out.println("=== 계좌 생성===");

        // 1. 사용자 입력 및 정규식 검증
        String accNum = getValidInput(
                "계좌번호를 입력하세요 (형식: 1234-1234): ",
                "^\\d{4}-\\d{4}$",
                "잘못된 형식입니다. '1234-1234' 형태로 입력해주세요."
        );

        String owner = getValidInput(
                "소유자 이름을 입력하세요 (한글 또는 영문): ",
                "^[가-힣a-zA-Z]+$",
                "이름은 한글 또는 영문만 가능합니다."
        );

        // 잔액은 정수만 입력받음
        String balanceStr = getValidInput(
                "잔액을 입력하세요 (숫자만): ",
                "^[0-9]+$",
                "0 이상의 숫자만 입력해주세요."
        );
        int balance = Integer.parseInt(balanceStr);

        String password = getValidInput(
                "비밀번호를 입력하세요 (4자리 이상): ",
                "^.{4,}$",
                "비밀번호는 최소 4자리 이상이어야 합니다."
        );

        // 2. Account 객체 생성
        Account myAccount = new Account(accNum, owner, balance, password);

        // 3. 파일 저장
        saveAccountToFile(myAccount, "account_info.txt");

        return new Account(accountNumber, owner, balance, password);
    }

    private void saveAccountToFile(Account account, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(account.getAccountNumber() + account.getOwner() + account.getBalance()+ account.getPassword());
            writer.newLine();

            System.out.println("✅ 파일 저장이 완료되었습니다: " + new File(fileName).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
}

// 2. 실행 및 파일 저장 클래스
public class registerCode {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

    }

    /**
     * 정규식을 사용하여 사용자 입력을 검증하는 헬퍼 메서드
     * @param prompt 사용자에게 보여줄 메시지
     * @param regex 검증할 정규표현식
     * @param errorMsg 검증 실패 시 보여줄 메시지
     * @return 검증된 입력 문자열
     */
    private static String getValidInput(String prompt, String regex, String errorMsg) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (Pattern.matches(regex, input)) {
                return input;
            } else {
                System.out.println("⚠️ " + errorMsg);
            }
        }
    }

    /**
     * Account 객체의 정보를 파일에 저장하는 메서드
     */

}
