package cn.hacktons.animation;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by chaobin on 9/5/17.
 */
public class XmlCompat {

    public static Drawable getDrawable(Resources res, TypedArray array, int index) {
        TypedValue value = new TypedValue();
        array.getValue(index, value);
        Drawable drawable = load(value, res);

        return drawable;
    }

    public static Drawable load(TypedValue value, Resources res) {
        try {
            XmlResourceParser parser = loadDrawableParser(value, value.resourceId, res);
            Drawable drawable = createFromXml(res, parser, null);
            parser.close();
            return drawable;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable createFromXml(Resources r, XmlPullParser parser, Resources.Theme theme)
        throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG &&
            type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        Drawable drawable = createFromXmlInner(r, parser, attrs, theme);

        if (drawable == null) {
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }

        return drawable;
    }

    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs,
                                              Resources.Theme theme) throws XmlPullParserException, IOException {
        final Drawable drawable;

        final String name = parser.getName();
        if (name.equals("animation-list")) {
            drawable = new MFAnimationDrawable();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = Drawable.createFromXmlInner(r, parser, attrs, theme);
            } else {
                drawable = Drawable.createFromXmlInner(r, parser, attrs);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.inflate(r, parser, attrs, theme);
        } else {
            drawable.inflate(r, parser, attrs);
        }
        return drawable;
    }

    private static XmlResourceParser loadDrawableParser(TypedValue value, int id, Resources resources)
        throws
        NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String file = value.string.toString();
        if (file.endsWith(".xml")) {
            Method loadXmlResourceParserMethod = Resources.class.getDeclaredMethod
                ("loadXmlResourceParser", String.class, int
                        .class,
                    int.class, String.class);
            loadXmlResourceParserMethod.setAccessible(true);
            final XmlResourceParser rp = (XmlResourceParser) loadXmlResourceParserMethod.invoke(resources, file,
                id, value.assetCookie, "drawable");
            return rp;
        } else {
            throw new IllegalArgumentException("Un-support value");
        }
    }


}
