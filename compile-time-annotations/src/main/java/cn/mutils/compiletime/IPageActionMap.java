package cn.mutils.compiletime;

import proguard.annotation.KeepImplementations;
import proguard.annotation.KeepName;

/**
 * Created by wenhua.ywh on 2016/8/5.
 */
@KeepName
@KeepImplementations
public interface IPageActionMap {
    Class<?> getPage(String action);
}
