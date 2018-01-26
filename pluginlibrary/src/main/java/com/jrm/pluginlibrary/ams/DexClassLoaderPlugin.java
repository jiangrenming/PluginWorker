package com.jrm.pluginlibrary.ams;

import android.content.Context;

import com.jrm.pluginlibrary.PluginFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 */

public class DexClassLoaderPlugin {


    public  static void inject(DexClassLoader loader) throws Exception{
        //此classLoader是加载宿主本身应用的dex文件的
        PathClassLoader pathClassLoader = (PathClassLoader) PluginFactory.getmContext().getClassLoader();
        //获取宿主的pathList
        Object pathListClassLoader = getPathListClassLoader(pathClassLoader);
        //获取插件apk的pathList
        Object dexClassList = getPathListClassLoader(loader);
        Object combineArray = combineArray(getElementArray(pathListClassLoader)
                , getElementArray(dexClassList));
        //重新将组合的dexElements对象复制到宿主应用里
        setField(pathListClassLoader,pathListClassLoader.getClass(),"dexElements",combineArray);
    }


    /**
     * 获取DexElements对象
     * @param paramObject
     * @return
     */
    private  static  Object getElementArray(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    /**
     * 组合宿主与apk插件dex的数组长度
     * @param pathList
     * @param dexList
     * @return
     */
    private  static Object combineArray(Object pathList,Object dexList){
        //获取原数组的类型
        Class<?> componentType = pathList.getClass().getComponentType();
        //获取原数组长度
        int path_length = Array.getLength(pathList);
        //新的组合数组的长度
        int j = path_length + Array.getLength(dexList);
        //创建一个新的数组来存储信息
        Object resut = Array.newInstance(componentType, j);
        for (int m = 0 ;m < j ;++m){
            if (m < path_length){
              Array.set(resut,m,Array.get(pathList,m));
            }else {
                Array.set(resut,m,Array.get(dexList,m-path_length));
            }
        }
        return  resut;
    }



    /**
     * 获取pathList字段
     * @param baseDexClassLoader
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private  static Object getPathListClassLoader(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        //通过这个ClassLoader获取pathList字段
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射获取字段的对象
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        //反射需要获取的字段
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    /**
     * 反射需要设置字段的类并设置新字段
     * @param obj
     * @param cl
     * @param field
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void setField(Object obj, Class<?> cl, String field,
                                 Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

}
