package com.telenor.possumlib.managers;

import com.telenor.possumlib.PossumTestRunner;

import org.junit.runner.RunWith;

@RunWith(PossumTestRunner.class)
public class SecretKeyTest {

//    @Test
//    public void ensureConsistentEncoding() {
//        for (int x = 0; x < 64; x++) {
//            assertEquals(
//                    READABLE_ENCODER[x],
//                    encodeReadably(new byte[]{0, 0, (byte) x}).charAt(3));
//        }
//    }
//
//    @Test
//    public void ensureChecksumFindsTypo() {
//        for (int i = 0; i < 100; i++) {
//            String key = createSecretKey();
//            char checksum = key.charAt(key.length() - 1);
//            for (int j = 0; j < 20; j++) {
//                char c = key.charAt(j);
//                for (int k = 0; k < 64; k++) {
//                    char altC = READABLE_ENCODER[k];
//                    if (altC != c) {
//                        // Change one character
//                        String altSecretPart = key.substring(0, j) + altC + key.substring(j + 1, key.length() - 1);
//                        assertEquals(key.length() - 1, altSecretPart.length());
//                        assertNotSame(checksum, checksum(altSecretPart));
//                    }
//                }
//                if (j < 19 && key.charAt(j + 1) != c) {
//                    // Flip two characters
//                    String altSecretPart = key.substring(0, j) + key.charAt(j + 1) + c + key.substring(j + 2, key.length() - 1);
//                    assertEquals(key.length() - 1, altSecretPart.length());
//                    assertNotSame(key + " " + j, checksum, checksum(altSecretPart));
//                }
//            }
//        }
//    }
}