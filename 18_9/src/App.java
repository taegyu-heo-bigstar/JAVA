public class App {
    public static void main(String[] args) throws Exception {
        Subscriberinfo obj1, obj2;

        Subscriberinfo test; 
        // test = new Subscriberinfo();
        test = testFunc();
        obj1 = new Subscriberinfo("heug", "p", "z");
        obj2 = new Subscriberinfo("nool", "r", "m", "000", "hhh");

        obj1.showVal_1();
        obj2.showVal_2();
    }
}

class Subscriberinfo{
    String name;
    String aespa;
    String favorits;
    String account;
    String building;

    Subscriberinfo() {
        System.out.println("기본 생성자 호출 성공");
    }

    Subscriberinfo testFunc(){
        Subscriberinfo obj;
        obj = new Subscriberinfo();
        return obj;
    }

    Subscriberinfo(String name, String aespa, String favorits) 
    {
        this.name = name;
        this.aespa = aespa;
        this.favorits = favorits;
    }

    Subscriberinfo(String name, String aespa, String favorits, String account, String building) 
    {
        this.name = name;
        this.aespa = aespa;
        this.favorits = favorits;
        this.account = account;
        this.building = building;
    }

    void showVal_1()
    {
        System.out.println(name + aespa + favorits);
    }

    void showVal_2()
    {
        System.out.println(name + aespa + favorits + account + building);
    }
}

