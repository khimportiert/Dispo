public class Time implements Comparable<Time> {
    int time;
    static int currentTime = 0;

    public Time() {
        this.time = 0;
    }

    public Time(int time) {
        this.time = time;
    }

    static void update() {
        currentTime++;
    }

    void add(Time time) {
        this.time += time.time;
    }

    static Time generateRandomTime() {
        return new Time((int) (Math.random() * 100));
    }

    static Time generateRandomTime(int max) {
        return new Time((int) (Math.random() * max));
    }

    static Time generateRandomTime(int min, int max) {
        return new Time((int) (Math.random() * max + min));
    }

    @Override
    public int compareTo(Time that) {
        return Integer.compare(this.time, that.time);
    }

    @Override
    public String toString() {
        return time+"min";
    }
}
