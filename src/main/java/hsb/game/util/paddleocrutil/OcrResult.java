package hsb.game.util.paddleocrutil;

import java.awt.*;

/**
 * @author 胡帅博
 * @date 2023/7/6 13:34
 */
public class OcrResult {

    public Rectangle rect;
    public String text;


    public OcrResult(Rectangle rect, String text) {
        this.rect = rect;
        this.text = text;
    }
}
