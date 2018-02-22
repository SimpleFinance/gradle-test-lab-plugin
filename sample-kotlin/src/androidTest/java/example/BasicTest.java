package example;

import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJunit4.class)
@SmallTest
public class BasicTest {
    @Test
    public void hello() {
        System.out.println("Hello world!");
    }
}
