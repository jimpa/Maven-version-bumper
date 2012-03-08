/*
 * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package se.tla.mavenversionbumper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Tests of the ReadonlyModule class.
 */
public class ReadonlyModuleTest {

    public static final String GROUP_ID = "groupId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String VERSION = "1.0";
    private ReadonlyModule subject;

    @Before
    public void before() {
        subject = new ReadonlyModule(GROUP_ID, ARTIFACT_ID, VERSION);
    }

    @Test
    public void testWorkingMethods() {
        Assert.assertEquals(GROUP_ID, subject.groupId());
        Assert.assertEquals(ARTIFACT_ID, subject.artifactId());
        Assert.assertEquals(VERSION, subject.version());
        Assert.assertEquals(GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION, subject.gav());
        Assert.assertEquals(GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION, subject.toString());
    }

    // TODO Fix this test. Almost works.
    //@Test
    public void testUnsupportedMethods() {

        for (Method method : subject.getClass().getMethods()) {
            String name = method.getName();
            if ("groupId".equals(name) ||
                "artifactId".equals(name) ||
                "version".equals(name) ||
                "gav".equals(name) ||
                "toString".equals(name)) {
                continue;
            }

            Class[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = null;
            }

            System.out.println("name : " + method.getName());
            try {
                if (parameters.length > 0) {
                    method.invoke(subject, parameters);
                } else {
                    method.invoke(subject);
                }
                Assert.fail();
            } catch (IllegalAccessException e) {
                Assert.fail(e.getMessage());
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (! (t instanceof UnsupportedOperationException)) {
                    Assert.fail();
                }
            }
        }
    }
}
