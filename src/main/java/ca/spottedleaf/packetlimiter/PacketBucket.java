package ca.spottedleaf.packetlimiter;

import java.util.Arrays;

public class PacketBucket {

    /** multiplier to convert ns to ms */
    private static final double NANOSECONDS_TO_MILLISECONDS = 1.0e-6; // 1e3 / 1e9

    /** divisor to convert ms to s */
    private static final int MILLISECONDS_TO_SECONDS = 1000;

    /**
     * The time frame this packet limiter will count packets over (ms).
     */
    public final double intervalTime;

    /**
     * The time each bucket will represent (ms).
     */
    public final double intervalResolution;

    /**
     * Total buckets contained in this limiter.
     */
    public final int totalBuckets;

    /** contains all packet data, note that indices of buckets will wrap around the array */
    private final int[] data;

    /** pointer which represents the bucket containing the newest data */
    private int newestData;

    /** the time attached to the bucket at newestData */
    private double lastBucketTime;

    /** cached sum of all data */
    private int sum;

    /**
     * Constructs a packetlimiter which will record total packets sent over the specified
     * interval time (in ms) with the specified number of buckets
     * @param intervalTime The specified interval time, in ms
     * @param totalBuckets The total number of buckets
     */
    public PacketBucket(final double intervalTime, final int totalBuckets) {
        this.intervalTime = intervalTime;
        this.intervalResolution = intervalTime/(double)totalBuckets;
        this.totalBuckets = totalBuckets;
        this.data = new int[totalBuckets];
    }

    /*
     * Stores number of packets according to their relative time. Each "bucket" will represent a time frame, according
     * to this.intervalResolution. The newestData pointer represents the bucket containing the newest piece of data.
     * The oldest piece of data is always the bucket after the newest data. When a new time frame is needed, the newest
     * data pointer is simply moved forward and previous data is either removed or overridden.
     *
     * For example, the following piece of code will sum all data older than the newest (starting from new -> older):
     *
     * int sum = 0;
     * for (int i = (newestData - 1) % buckets; i != newestData; i = (i - 1) % total_buckets) {
     *     sum += buckets[i];
     * }
     *
     * Additionally, the sum of data is cached. Older data is automatically subtracted and newer data is automatically
     * summed. This makes calls made within a time interval of 'this.intervalResolution' O(1)
     *
     * Calls made over larger timers are O(n), with n ~ min(total_buckets, timePassed / bucket_interval)
     */

    /**
     * Adds to this limiter's packet count. Old data is automatically purged, and the current time from {@link System#nanoTime()}
     * is used to record this data. Returns the new packet count.
     * @param packets The number of packets to attach to the current time
     * @return The new packet count
     */
    public int incrementPackets(final int packets) {
        return this.incrementPackets(System.nanoTime(), packets);
    }

    private int incrementPackets(final long currentTime, final int packets) {
        final double timeMs = currentTime * NANOSECONDS_TO_MILLISECONDS;
        double timeDelta = timeMs - this.lastBucketTime;

        if (timeDelta < 0.0) {
            // we presume the difference is small. nanotime always moves forward
            timeDelta = 0.0;
        }

        if (timeDelta < this.intervalResolution) {
            this.data[this.newestData] += packets;
            return this.sum += packets;
        }

        final int bucketsToMove = (int)(timeDelta / this.intervalResolution);

        // With this expression there will be error, however it is small and will continue to the next counter.
        // In the end what matters is the DIFFERENCE in time between each bucket being the interval resolution,
        // which will remain approximately the case for this class' use case.
        // For large bucket counts (n > 1_000_000) we might have to consider the error.
        final double nextBucketTime = this.lastBucketTime + bucketsToMove * this.intervalResolution;

        if (bucketsToMove >= this.totalBuckets) {
            // we need to simply clear all data
            Arrays.fill(this.data, 0);

            this.data[0] = packets;
            this.sum = packets;
            this.newestData = 0;
            this.lastBucketTime = timeMs;

            return packets;
        }

        // buckets we have no data for (since we've jumped ahead of them)
        for (int i = 1; i < bucketsToMove; ++i) {
            final int index = (this.newestData + i) % this.totalBuckets;
            this.sum -= this.data[index];
            this.data[index] = 0;
        }

        final int newestDataIndex = (this.newestData + bucketsToMove) % this.totalBuckets;
        this.sum += packets - this.data[newestDataIndex]; // this.sum += packets; this.sum -= this.data[index]
        this.data[newestDataIndex] = packets;
        this.newestData = newestDataIndex;
        this.lastBucketTime = nextBucketTime;

        return this.sum;
    }

    /**
     * Returns the total number of packets recorded in this interval.
     */
    public int getTotalPackets() {
        return this.sum;
    }

    /**
     * Returns the current packet rate (since last update call), in packets per second
     */
    public double getCurrentPacketRate() {
        return this.sum / (this.intervalTime / (double)MILLISECONDS_TO_SECONDS);
    }
}
