import java.io.*;
import java.util.Scanner;

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
        String filePath = "accounts.txt"; // 예시 파일 경로
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
        String filePath = "accounts.txt"; // 예시 파일 경로
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
        String filePath = "accounts.txt"; // 예시 파일 경로
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
}

public class Main {
    public static void main(String[] args) {
        // [변경 1] 파일에서 읽어올 것이므로 넉넉한 크기로 배열 생성 (최대 100명)
        Account acc[] = new Account[100];
        int count = 0; // 실제 등록된 계좌 수를 카운트할 변수

        // [변경 2] 파일 입출력을 통한 계좌 등록 로직
        try {
            File file = new File("accounts.txt"); // 읽어올 파일 객체 생성
            Scanner fileScan = new Scanner(file);

            while (fileScan.hasNext()) {
                String code = fileScan.next();
                String owner = fileScan.next();
                int bal = fileScan.nextInt();
                String pw = fileScan.next();

                // 배열에 객체 생성하여 저장 및 카운트 증가
                acc[count] = new Account(code, owner, bal, pw);
                count++;
            }
            fileScan.close();
            System.out.println("시스템: " + count + "개의 계좌 정보를 파일에서 로드했습니다.");

        } catch (FileNotFoundException e) {
            System.out.println("오류: accounts.txt 파일을 찾을 수 없습니다.");
            return; // 파일이 없으면 프로그램 종료
        } catch (Exception e) {
            System.out.println("오류: 파일 읽기 중 문제가 발생했습니다.");
            e.printStackTrace();
            return;
        }

        Scanner scan = new Scanner(System.in);
        String usercode;

        while (true) {
            System.out.print("계좌번호 : ");
            usercode = scan.next();

            // [변경 3] 기존의 숫자 3 대신 실제 로드된 개수(count)를 전달
            int idx = Account.find(acc, count, usercode);

            if (idx >= 0) {
                int select = 0, amnt;
                String code, pw;
                System.out.println(acc[idx].getOwner() + "님 환영합니다");
                while (select != 5) {
                    System.out.print("1.입금 2.출금 3.송금 4.조회 5.종료 : ");
                    select = scan.nextInt();
                    switch (select) {
                        case 1:
                            System.out.print("입금액 : ");
                            amnt = scan.nextInt();
                            if (acc[idx].deposit(amnt)) {
                                System.out.println("입금완료되었습니다. 잔액:" + acc[idx].getBal());
                            } else {
                                System.out.println("입금실패하였습니다");
                            }
                            break;
                        case 2:
                            System.out.print("출금액 : ");
                            amnt = scan.nextInt();
                            System.out.print("비밀번호 : ");
                            pw = scan.next();
                            if (acc[idx].withdraw(amnt, pw)) {
                                System.out.println("출금완료되었습니다. 잔액:" + acc[idx].getBal());
                            } else {
                                System.out.println("출금실패하였습니다");
                            }
                            break;
                        case 3:
                            System.out.print("송금계좌 : ");
                            code = scan.next();
                            System.out.print("출금액 : ");
                            amnt = scan.nextInt();
                            System.out.print("비밀번호 : ");
                            pw = scan.next();
                            // [변경 4] 송금 대상 찾기에서도 count 변수 사용
                            int tidx = Account.find(acc, count, code);
                            if (tidx < 0) {
                                System.out.println("등록된계좌가 아닙니다");
                            } else {
                                if (acc[idx].transfer(acc[tidx], amnt, pw)) {
                                    System.out.println("송금완료되었습니다. 잔액:" + acc[idx].getBal());
                                } else {
                                    System.out.println("송금실패하였습니다");
                                }
                            }
                            break;
                        case 4:
                            // 기존 코드에 4번(조회) 로직이 비어있어서 추가해 드립니다.
                            System.out.print("비밀번호 : ");
                            pw = scan.next();
                            if(!acc[idx].show(pw)) {
                                System.out.println("비밀번호가 일치하지 않습니다.");
                            }
                            break;
                        case 5:
                            break;
                    }
                }
            } else if (usercode.equals("bye")) {
                System.out.println("프로그램 종료");
                break;
            } else {
                System.out.println("등록되지않은 계좌입니다");
            }
        }
    }
}