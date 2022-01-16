package dev.westernpine.lib.interaction;

public abstract interface CommandHandler {

    /**
     * @return How the command should be used.
     */
    public String[] usages();

    /**
     * @return The command signifier string.
     */
    public String command();

    /**
     * @return The command description.
     */
    public String description();

}
