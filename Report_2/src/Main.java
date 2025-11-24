
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;


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

public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Account acc[] = new Account[3];
		acc[0] = new Account("1234-1234", "장용훈", 100000, "1234");
		acc[1] = new Account("4321-4321", "홍길동", 100000, "4321");
		acc[2] = new Account("1111-1111", "이순신", 100000, "1111");
		
		Scanner scan = new Scanner(System.in);
		String usercode;
		
		while(true)
		{
			System.out.print("계좌번호 : ");
			usercode = scan.next();
			int idx = Account.find(acc, 3, usercode);
			if(idx >= 0) // <<-배열속에서 계좌번호 찾는 코드로변경
			{
				int select=0, amnt;
				String code, pw;
				
				System.out.println(acc[idx].getOwner() + "님 환영합니다");
				while(select != 5)
				{
					System.out.print("1.입금 2.출금 3.송금 4.조회 5.종료 : ");
					select = scan.nextInt();
					switch(select)
					{
					case 1: 
						System.out.print("입금액 : ");
						amnt = scan.nextInt();
						if(acc[idx].deposit(amnt))
						{
							System.out.println("입금완료되었습니다. 잔액:"+acc[idx].getBal());
							
						}
						else {
							System.out.println("입금실패하였습니다");
						}
						break;
					case 2: 
						System.out.print("출금액 : ");
						amnt = scan.nextInt();
						System.out.print("비밀번호 : ");
						pw = scan.next();
						if(acc[idx].withdraw(amnt, pw))
						{
							System.out.println("출금완료되었습니다. 잔액:" + acc[idx].getBal());
						}
						else {
							System.out.println("출금실패하였습니다");
						}
						break;
		
					case 3: 
						System.out.print("송금계좌 : ");
						code= scan.next();
						System.out.print("출금액 : ");
						amnt = scan.nextInt();
						System.out.print("비밀번호 : ");
						pw = scan.next();
						
						int tidx = Account.find(acc, 3, code);
						if(tidx < 0 ) 
						{
							System.out.println("등록된계좌가 아닙니다");
						}
						else
						{
							if(acc[idx].transfer(acc[tidx], amnt, pw))
							{
								System.out.println("송금완료되었습니다. 잔액:" + acc[idx].getBal());
							}
							else {
								System.out.println("송금실패하였습니다");
							}
						}
						break;
					case 4: break;
					case 5: break;
					}
				}
			}
			else if(usercode.equals("bye"))
			{
				System.out.println("프로그램 종료");
				break;
			}
			else
			{
				System.out.println("등록되지않은 계좌입니다");
			}
		}
		
		
	}

}
