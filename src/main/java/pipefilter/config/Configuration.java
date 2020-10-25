package pipefilter.config;

public class Configuration {
    /**
     * The sentinel value signals the end of the text stream.
     * The value is a random string generated with the openssl tool to make
     * the chances of having the same value in the actual text stream slim.
     *
     *     $ openssl rand -base64 32
     */
    public static final String SENTINEL_VALUE = "ZTmlDP63gcm0d/LvvLdf4tHrtFl1rkc79IAVucfa3/A=";
    /**
     * The capacity of the pipes.
     *  - Same for all pipes
     *  - Value arbitrarily chosen for now
     */
    public static final int PIPE_CAPACITY = 100;
}
