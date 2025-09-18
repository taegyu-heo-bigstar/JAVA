public class tv {
    public static void main(String[] args) {
        tv TV;
        TV = new tv(false, 0, 0);

        tv test;
        tv.createTest(test);

        TV.onOff();
        TV.voUp();
    }

    boolean power;
    int channel;
    int volume;

    static tv create()
    {
        return new tv(false, 0, 0);
    }

    static tv createTest(tv TV)
    {
        TV = new tv(false, 0, 0);
        return TV;
    }

    tv(boolean powerState, int channelNum, int volumeNum)
    {
        power = powerState;
        channel = channelNum;
        volume = volumeNum;
    }

    boolean onOff()
    {
        this.power = !this.power;
        return (this.power);
    }

    int chUp()
    {
        this.channel++;
        return(this.channel);
    }

    int chDw()
    {
        this.channel--;
        return(this.channel);
    }

    int voUp()
    {
        this.volume++;
        return (this.volume);
    }

    int voDW()
    {
        this.volume--;
        return (this.volume);
    }
}
