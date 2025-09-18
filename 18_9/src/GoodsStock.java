class GoodsStock {
    String goodsCode;
    int stockNum;
    void addStock(int amount)
    {
        stockNum += amount;
    }
    int subtractStock(int amount)
    {
        if (stockNum < amount) return 0;
        stockNum -= amount;
        return stockNum;
    }

    GoodsStock(String code, int amount)
    {
        goodsCode = code;
        stockNum = amount;
    }

    void showStock()
    {
        System.out.println("goods code " + goodsCode);
        System.out.println("stockNum " + stockNum);
    }

    public static boolean isSameType(Object a, Object b) {
        if (a == null || b == null) return false; // null이면 비교 불가
        return a.getClass().equals(b.getClass());
    }

    public static void main(String[] args) {
        GoodsStock obj;
        obj = new GoodsStock("1101", 100);
        // obj.goodsCode = "1101";
        // obj.stockNum = 100;

        System.out.println("item code " + obj.goodsCode);
        System.out.println("item amount " + obj.stockNum);

        obj.addStock(200);

        System.out.println("item code " + obj.goodsCode);
        System.out.println("item amount " + obj.stockNum);

        obj.subtractStock(100);
        obj.showStock();

    }
}
