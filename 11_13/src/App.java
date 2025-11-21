import java.util.Scanner;

interface moneyControlable {
    void deposit(int amount);
    boolean withdraw(int amount, String password);
    boolean transfer(Account to, int amount, String password);
    int checkBalance(String password);
    void exit();
}

class Account implements moneyControlable {
    private String accountNumber;
    private int balance;
    private String owner;
    private String password;
    private boolean active = true;

    public Account(String owner, String password, int initialBalance, String accountNumber) {
        this.owner = owner;
        this.password = password;
        this.balance = Math.max(0, initialBalance);
        this.accountNumber = accountNumber;
    }

    @Override
    public void deposit(int amount) {
        if (!active) return;
        if (amount <= 0) return;
        balance += amount;
    }

    @Override
    public boolean withdraw(int amount, String password) {
        if (!active) return false;
        if (!this.password.equals(password)) return false;
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        return true;
    }

    @Override
    public boolean transfer(Account to, int amount, String password) {
        if (!active) return false;
        if (to == null) return false;
        if (this.withdraw(amount, password)) {
            to.deposit(amount);
            return true;
        }
        return false;
    }

    @Override
    public int checkBalance(String password) {
        if (!active) return -1;
        if (!this.password.equals(password)) return -1;
        return balance;
    }

    @Override
    public void exit() {
        active = false;
    }

    public String getOwner() { return owner; }
    public boolean isActive() { return active; }
    public String getAccountNumber() { return accountNumber; }
}

class User {
    private String name;
    private Account account;

    public User(String name, Scanner scanner) {
        this.name = name;
        System.out.println(this.name + "의 계좌 번호 입력 :");
        String accountNumber = scanner.next();
        System.out.println(this.name + "의 계좌 비밀번호 입력 :");
        String password = scanner.next();
        this.account = new Account(name, password, 0, accountNumber);
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }
}

public class App {
    public static boolean selectAndrunMenu(User[] users, User user, Scanner scanner) {
    int optionNumber;
    String optionNumberStr;
    String password = "";
    System.out.println("\n=== " + user.getName() + "님, 원하시는 메뉴를 선택하세요 ===");
    System.out.println("1. 입금 2. 출금 3. 이체 4. 잔액조회 5. 다른 계좌 선택");
    optionNumberStr = scanner.next();
    if (!isInteger(optionNumberStr))
        return false;
    optionNumber = Integer.parseInt(optionNumberStr);
    if (optionNumber >= 1 && optionNumber <= 4) {
        switch(optionNumber) {
            case 1:
                System.out.println("입금할 금액: ");
                int depositAmount = scanner.nextInt();
                user.getAccount().deposit(depositAmount);
                System.out.println(depositAmount + "원이 입금되었습니다.");
                break;
            case 2:
                System.out.println("비밀번호 입력: ");
                password = scanner.next();
                System.out.println("출금할 금액: ");
                int withdrawAmount = scanner.nextInt();
                if (user.getAccount().withdraw(withdrawAmount, password)) {
                    System.out.println(withdrawAmount + "원이 출금되었습니다.");
                } else {
                    System.out.println("출금에 실패했습니다. (비밀번호 불일치 또는 잔액 부족)");
                }
                break;
            case 3:
                System.out.println("비밀번호 입력: ");
                password = scanner.next();
                System.out.println("이체할 금액: ");
                int transferAmount = scanner.nextInt();
                System.out.println("이체할 계좌 번호: ");
                String toAccountNum = scanner.next();
                Account toAccount = findAccountByAccountNumber(users, toAccountNum);
                if (toAccount == null) {
                    System.out.println("상대방 계좌를 찾을 수 없습니다.");
                    break;
                }
                if (user.getAccount().transfer(toAccount, transferAmount, password)) {
                    System.out.println(transferAmount + "원이 " + toAccountNum + "로 이체되었습니다.");
                } else {
                    System.out.println("이체에 실패했습니다. (비밀번호 불일치 또는 잔액 부족)");
                }
                break;
            case 4:
                 System.out.println("비밀번호 입력: ");
                 password = scanner.next();
                 int balance = user.getAccount().checkBalance(password);
                 if (balance != -1) {
                     System.out.println("현재 잔액은 " + balance + "원 입니다.");
                 } else {
                     System.out.println("비밀번호가 틀렸거나 계좌가 비활성 상태입니다.");
                 }
                break;
        }
        return true; // 메뉴 실행 후 계속 진행
    } else if (optionNumber == 5) {
        System.out.println("다른 계좌를 선택합니다.");
        return true; // 계좌 선택 루프로 돌아감
    } else {
        System.out.println("잘못된 선택입니다. 프로그램을 종료합니다.");
        return false; // 잘못된 입력 시 종료
    }
}

    public static int findUserByAccountNumber(User[] users, String accountNumber) {
        for (int i = 0; i < users.length; i++) {
            if (users[i] != null && users[i].getAccount() != null && users[i].getAccount().getAccountNumber().equals(accountNumber)) {
                return i;
            }
        }
        return -1; // not found
    }
    
    public static Account findAccountByAccountNumber(User users[], String accountNumber) {
        for (User user : users) {
            if (user != null && user.getAccount() != null && user.getAccount().getAccountNumber().equals(accountNumber)) {
                return user.getAccount();
            }
        }
        return null; // not found
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 사용자 계좌를 설정합니다. ===");
        User user1 = new User("Alice", scanner);
        User user2 = new User("Bob", scanner);
        User user3 = new User("Charlie", scanner);
        User users[] = {user1, user2, user3}; 
        boolean checkFlag = true;

        while(checkFlag)
        {
            System.out.println("\n=== 계좌를 선택해주세요. ===");
            System.out.println("계좌 번호 (종료하려면 'exit' 입력): ");
            String accountNumber = scanner.next();

            if ("exit".equalsIgnoreCase(accountNumber)) {
                System.out.println("프로그램을 종료합니다.");
                break;
            }

            int userIndex = findUserByAccountNumber(users, accountNumber);
            if (userIndex == -1) {
                System.out.println("해당 계좌가 없습니다. 다시 입력해주세요.");
            } else {
                User currentUser = users[userIndex];
                checkFlag = selectAndrunMenu(users, currentUser, scanner);          
            }
        }
        scanner.close();
    }
}
