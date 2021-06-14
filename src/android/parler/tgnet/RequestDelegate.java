package org.parler.tgnet;

public interface RequestDelegate {
    void run(TLObject response, TLRPC.TL_error error);
}
