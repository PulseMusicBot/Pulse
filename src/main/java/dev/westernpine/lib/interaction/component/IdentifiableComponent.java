package dev.westernpine.lib.interaction.component;

public interface IdentifiableComponent extends Comparable<IdentifiableComponent> {

    public String id();

    public default int compareTo(IdentifiableComponent identifiableComponent) {
        return id().compareTo(identifiableComponent.id());
    }

}
