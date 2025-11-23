import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

// 1. Account 객체 정의
class Account
{
	private String code, own, pw;
	private int bal;
	Account(String code, String own, int bal, String pw)
	{
		this.code = new String(code);
		this.own = new String(own);
		this.bal = bal;
		this.pw = new String(pw);
	}
	boolean deposit(int amnt)
	{
		if(amnt<0) return false;
		bal += amnt;
		return true;
	}
	boolean withdraw(int amnt, String pw)
	{
		if(!this.pw.equals(pw)) return false;
		if(this.bal < amnt) return false;
		bal -= amnt;
		return true;
	}
	boolean transfer(Account acc, int amnt, String pw)
	{
		if(!withdraw(amnt, pw)) return false;
		return acc.deposit(amnt);
	}
	boolean show(String pw)
	{
		if(!this.pw.equals(pw)) return false;
		System.out.println(own+"님의 잔액 : " + bal);
		return true;
	}
	String getOwner()
	{
		return own;
	}
	int getBal()
	{
		return bal;
	}
	static int find(Account acc[], int cnt, String code)
	{
		for(int i=0; i<cnt; i++)
		{
			if(acc[i].code.equals(code))
			{
				return i;
			}
		}
		return -1;
	}
    
    //추가된 시스템용 메서드
    boolean system_withdraw(int amnt, String pw) {
        if(!(pw == "admin")) return false;   // 관리자 권한 추가
        if(this.bal < amnt) return false;
        bal -= amnt;
        return true;
    }
    boolean system_transfer(Account acc, int amnt, String pw) {
        if(!(pw == "admin")) return false;   // 관리자 권한 추가
        if(this.bal < amnt) return false;
        bal -= amnt;
        return acc.deposit(amnt);
    }
    boolean system_deposit(int amnt, String pw) {
        if(!(pw == "admin")) return false;   // 관리자 권한 추가
        return deposit(amnt);
    }
    String getCode(String pw) {
        if (!this.pw.equals(pw)) return null;
        return this.code;
    }
    String getPassword(String pw) {
        if (!this.pw.equals(pw)) return null;
        return this.pw;
    }
}

class AccountManager {
    static Account createAccount(Scanner scanner) {
        System.out.println("=== 계좌 생성===");

        // 1. 사용자 입력 및 정규식 검증
        String accNum = getValidInput(
                scanner,
                "계좌번호를 입력하세요 (형식: 1234-1234): ",
                "^\\d{4}-\\d{4}$",
                "잘못된 형식입니다. '1234-1234' 형태로 입력해주세요."
        );

        String owner = getValidInput(
                scanner,
                "소유자 이름을 입력하세요 (한글 또는 영문): ",
                "^[가-힣a-zA-Z]+$",
                "이름은 한글 또는 영문만 가능합니다."
        );

        // 잔액은 정수만 입력받음
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

        // 2. Account 객체 생성
        Account myAccount = new Account(accNum, owner, balance, password);

        // 3. 파일 저장
        if (!saveAccountToFile(myAccount, "account_info.txt")) {
            System.out.println("계좌 생성에 실패했습니다.");
        } else {
            System.out.println("✅ 계좌가 성공적으로 생성되었습니다.");
        }

        return myAccount;
    }

    static boolean deposit(Account account, int amount) {
        if (account.deposit(amount)) {
            if (saveAccountToFile(account, "account_info.txt")) {
                System.out.println("입금이 완료되었습니다.");
                return true;
            } else {
                account.system_withdraw(amount, "admin"); // 롤백
                System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
                return false;
            }
        } else {
            System.out.println("❌ 입금에 실패했습니다.");
            return false;
        }
    }

    static boolean withdraw(Account account, int amount, String password) {
        if (account.withdraw(amount, password)) {
            if (saveAccountToFile(account, "account_info.txt")) {
                System.out.println("✅ 출금이 완료되었습니다.");
                return true;
            }
            else {
                System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
                account.system_deposit(amount, password); // 롤백
                return false;
            }
        } else {
            System.out.println("❌ 출금에 실패했습니다.");
            return false;
        }
    }

    static boolean transfer(Account fromAccount, Account toAccount, int amount, String password) {
        if (fromAccount.transfer(toAccount, amount, password)) {
            if (saveAccountToFile(fromAccount, "account_info.txt") &&
                saveAccountToFile(toAccount, "account_info.txt")) {
                System.out.println("송금에 성공하였습니다.");
                return true;
            }
            else {
                System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
                fromAccount.system_transfer(toAccount, amount, "admin"); // 롤백
                return false;
            }
        } else {
            System.out.println("❌ 이체에 실패했습니다.");
            return false;
        }
    }   

    private static boolean saveAccountToFile(Account account, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(account.getCode("admin") + " " +
                        account.getOwner() + " " + 
                        account.getBal()+ " " + 
                        account.getPassword("admin"));
            writer.newLine();

            System.out.println("✅ 파일 저장이 완료되었습니다: " + new File(fileName).getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("❌ 파일 저장 중 오류가 발생했습니다.");
            e.printStackTrace();
            return false;
        }
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

// 2. 실행 및 파일 저장 클래스
public class registerCode {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Account[] accounts = new Account[2];
        for (int i = 0; i < 2; i++) {
            accounts[i] = AccountManager.createAccount(scanner);
        }
        while (true) {
            System.out.print("계속 진행하시겠습니까? (y/n): ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("n")) {
                System.out.println("프로그램을 종료합니다.");
                break;
            } else if (!choice.equalsIgnoreCase("y")) {
                System.out.println("잘못된 입력입니다. 다시 시도해주세요.");
            }
            System.out.println("관리할 계좌의 계좌번호를 입력하세요. : ");
            String accountNumber = scanner.nextLine();
            int acc = Account.find(accounts, 2, accountNumber);
            Account selectedAccount = accounts[acc];
            System.out.println("1. 입금 2. 출금 3. 이체 4. 잔액 조회");
            int action = Integer.parseInt(scanner.nextLine());
            switch (action) {
                case 1:
                    System.out.print("입금할 금액을 입력하세요: ");
                    int depositAmount = Integer.parseInt(scanner.nextLine());
                    AccountManager.deposit(selectedAccount, depositAmount);
                    break;
                case 2:
                    System.out.print("출금할 금액을 입력하세요: ");
                    int withdrawAmount = Integer.parseInt(scanner.nextLine());
                    System.out.print("비밀번호를 입력하세요: ");
                    String withdrawPw = scanner.nextLine();
                    AccountManager.withdraw(selectedAccount, withdrawAmount, withdrawPw);
                    break;
                case 3:
                    System.out.print("이체할 금액을 입력하세요: ");
                    int transferAmount = Integer.parseInt(scanner.nextLine());
                    System.out.print("이체할 계좌 번호를 입력하세요 (1-5): ");
                    int toAccIndex = Integer.parseInt(scanner.nextLine()) - 1;
                    Account toAccount = accounts[toAccIndex];
                    System.out.print("비밀번호를 입력하세요: ");
                    String transferPw = scanner.nextLine();
                    AccountManager.transfer(selectedAccount, toAccount, transferAmount, transferPw);
                    break;
                case 4:
                    System.out.print("비밀번호를 입력하세요: ");
                    String showPw = scanner.nextLine();
                    selectedAccount.show(showPw);
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
                    break;
        }
        scanner.close();
        }
    }   
}
