package org.parler.tgnet;

public interface RequestDelegateInternal {
    void run(long response, int errorCode, String errorText, int networkType);
}
