public class Time implements Comparable<Time> {
    static Time currentTime;
    protected int hours;
    protected int minutes;

    public Time(int ticks) {
        validateInput(ticks);
        this.hours = ticks / 60;
        this.minutes = ticks % 60;
    }

    public Time(Time time) {
        this.hours = time.hours;
        this.minutes = time.minutes;
    }

    public Time(int hours, int minutes) {
        validateInput(hours, minutes);
        this.hours = hours;
        this.minutes = minutes;
    }

    static void initialize(int hours, int minutes) {
        currentTime = new Time(hours, minutes);
    }

    void setTime(int hours, int minutes) throws IllegalArgumentException {
        validateInput(hours, minutes);
        this.hours = hours;
        this.minutes = minutes;
    }

    protected String getTimeString() {
        return String.format("%02d", hours)+":"+String.format("%02d", minutes);
    }

    int getHours() {
        return hours;
    }

    int getMinutes() {
        return minutes;
    }

    Time addTime(Time toAdd) {
        this.hours += toAdd.hours;
        this.minutes += toAdd.minutes;
        reformat();
        return new Time(this.hours, this.minutes);
    }

    int calcTicks() {
        return hours * 60 + minutes;
    }

    static Time generateRandomTime(int min, int max) {
        int ticks = RandomInt.between(min, max);
        return new Time(ticks);
    }

    static Time generateRandomTime(Time min, Time max) {
        int hours = RandomInt.between(min.hours, max.hours);
        int minutes = RandomInt.between(min.minutes, max.minutes);
        return new Time(hours, minutes);
    }

    void reformat() {
        int minuteOverflow = this.minutes / 60;
        this.hours += minuteOverflow;
        this.minutes = this.minutes % 60;
    }

    void validateInput(int ticks) throws IllegalArgumentException {
        if (ticks < 0 || ticks > 60*24-1) {
            throw new IllegalArgumentException("Hours must be between 0 and 23");
        }
    }

    void validateInput(int hours, int minutes) throws IllegalArgumentException {
        if (hours < 0 || hours > 23) {
            throw new IllegalArgumentException("Hours must be between 0 and 23");
        }
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes must be between 0 and 59");
        }
    }

    @Override
    public int compareTo(Time that) {
        return Integer.compare(this.hours*60 + this.minutes, that.hours*60 + that.minutes);
    }

    @Override
    public String toString() {
        return getTimeString();
    }
}
