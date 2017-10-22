package org.avario.engine.tracks;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

import org.avario.utils.Logger;

public class SignedOutputStream extends BufferedOutputStream {
	protected Signature sign;

	public SignedOutputStream(OutputStream out, Signature sign) {
		super(out);
		this.sign = sign;
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		try {
			sign.update(buffer);
		} catch (SignatureException ex) {
			Logger.get().log("Fail counting signature ", ex);
		} finally {
			super.write(buffer);
		}
	}
}
