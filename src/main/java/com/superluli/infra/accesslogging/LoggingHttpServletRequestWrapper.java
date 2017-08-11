package com.superluli.infra.accesslogging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] bytes;

    public LoggingHttpServletRequestWrapper(HttpServletRequest request) throws IOException {

        super(request);
        bytes = IOUtils.toByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        
        return new ByteArrayServletInputStream(bytes);
        
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    static class ByteArrayServletInputStream extends ServletInputStream {

        private ByteArrayInputStream bais;

        public ByteArrayServletInputStream(byte[] bytes) {

            bais = new ByteArrayInputStream(bytes);
        }

        @Override
        public boolean isFinished() {

            return false;
        }

        @Override
        public boolean isReady() {

            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {

            return bais.read();
        }

    }

    /**
     * @return the bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
