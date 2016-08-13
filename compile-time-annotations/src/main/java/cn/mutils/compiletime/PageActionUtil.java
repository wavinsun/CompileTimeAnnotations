package cn.mutils.compiletime;

/**
 * Created by wenhua.ywh on 2016/8/8.
 */
public final class PageActionUtil {

    private static IPageActionMap sPageActionMap = getActionMap();

    private PageActionUtil() {

    }

    public static Class<?> getPage(String action) {
        return sPageActionMap != null ? sPageActionMap.getPage(action) : null;
    }

    public static IPageActionMap getActionMap() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(IPageActionMap.class.getPackage().getName());
            sb.append(".impl.");
            sb.append(IPageActionMap.class.getSimpleName().substring(1));
            sb.append("Impl");
            return (IPageActionMap) Class.forName(sb.toString()).newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}
