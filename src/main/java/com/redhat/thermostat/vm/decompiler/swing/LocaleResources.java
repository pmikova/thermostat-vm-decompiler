package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.shared.locale.Translate;

/**
 * Resource of names for the GUI tab and header.
 */
public enum LocaleResources {

    VM_DECOMPILER_TAB_NAME,
    DECOMPILER_HEADER_TITLE,;

    static final String RESOURCE_BUNDLE = "com.redhat.thermostat.vm.decompiler.swing.internal.strings";

    public static Translate<LocaleResources> createLocalizer() {
        return new Translate<>(RESOURCE_BUNDLE, LocaleResources.class);
    }
}
