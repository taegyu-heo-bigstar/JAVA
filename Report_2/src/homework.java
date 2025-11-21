import java.io.*;
import java.util.Scanner;

class Constants
{
    static final String filePath = "accounts.txt";
}

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

}

static class AccountManager {
    // 계좌 관리 관련 기능들을 이 클래스에 추가할 수 있습니다.
    public Account registerAccount(Scanner scan) {
        // 계좌 정보 입력 및 검증 로직
        String code, own, pw;
        int bal;
        System.out.print("계좌번호: ");
        code = scan.next();
        if (isAccountExists(code)) {
            System.out.println("이미 존재하는 계좌번호입니다.");
            return null;
        }
        System.out.print("예금주: ");
        own = scan.next();
        System.out.print("초기입금액: ");
        bal = scan.nextInt();
        System.out.print("비밀번호: ");
        pw = scan.next();
        if (!formCheck(code, own, bal, pw)) {
            System.out.println("입력형식이 올바르지 않습니다.");
            return null;
        }

        // 계좌 생성 및 저장 로직
        try {
            Account newAccount = new Account(code, own, bal, pw);
        } catch (IOException e) {
            System.out.println("계좌 생성에 실패했습니다.");
            e.printStackTrace();
            return null;
        }

        // 파일에 계좌 정보 저장 로직
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {

            String line = code + "," + own + "," + bal + "," + pw;

            bw.write(line); // 파일에 쓰기
            bw.newLine();   // 줄 바꿈 (\n)
        } catch (IOException e) {
            System.out.println("파일 쓰기 중 오류 발생. 계좌 생성에 실패했습니다.");
            e.printStackTrace();
            return null;
        }

        //
        return newAccount;
    }

    public boolean isAccountExists(String code) {
        // 계좌 존재 여부 확인 로직
        try (
            BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(code)) {
                    return true; // 계좌가 존재함
                }
            }
        } catch (IOException e) {
            System.out.println("파일 읽기 중 오류 발생.");         
            e.printStackTrace();
        }
        return false;
    }

    public Account getAccountByCode(String code) {
        if (!isAccountExists(code)) {
            return null; // 계좌가 존재하지 않음
        }

        // 계좌 정보 로드 로직
        try (
            BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(code)) {
                    String own = parts[1];
                    int bal = Integer.parseInt(parts[2]);
                    String pw = parts[3];
                    return new Account(code, own, bal, pw);
                }
            }
        } catch (IOException e) {
            System.out.println("파일 읽기 중 오류 발생.");         
            e.printStackTrace();
        }
        return null; // 계좌를 찾지 못한 경우
    }

    private boolean formCheck(String code, String own, int bal, String pw) {
        // 입력 형식 검사 로직

        if (code == null || code.isEmpty() || !code.matches("^\\d{4}-\\d{4}$")) return false;
        if (own == null || own.isEmpty() || !pw.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) return false;
        if (bal < 0) return false;
        if (pw == null || pw.isEmpty() || !pw.matches("^\\d{4}$")) return false;

        return true;
    }

    public void saveAccountToFile(Account account) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
                String line = account.getCode() + "," + account.getOwner() + "," + account.getBal() + "," + account.getPw();
                bw.write(line);
                bw.newLine();
        } catch (IOException e) {
            System.out.println("파일 쓰기 중 오류 발생.");
            e.printStackTrace();
        }
    }

    public void depositToAccount(String code, int amount) {
        Account account = getAccountByCode(code);
        if (account != null) {
            account.deposit(amount);
            saveAccountToFile(account);
        }
    }

    public void withdrawFromAccount(String code, int amount, String pw) {
        Account account = getAccountByCode(code);
        if (account != null) {
            account.withdraw(amount, pw);
            saveAccountToFile(account);
        }
    }

    public void transferBetweenAccounts(String fromCode, String toCode, int amount, String pw) {
        Account fromAccount = getAccountByCode(fromCode);
        Account toAccount = getAccountByCode(toCode);
        if (fromAccount != null && toAccount != null) {
            fromAccount.transfer(toAccount, amount, pw);
            saveAccountToFile(fromAccount);
            saveAccountToFile(toAccount);
        }
    }

}

public class Main {
	public static void main(String[] args) {
        // 프로그램 실행 로직

    }   
}
