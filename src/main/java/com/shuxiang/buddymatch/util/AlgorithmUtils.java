package com.shuxiang.buddymatch.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AlgorithmUtils {



    /**
     * 编辑距离算法（用于计算最相似的两组标签）
     * 原理：https://blog.csdn.net/DBC_121/article/details/104198838
     *
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static double dice(List<String> list1, List<String> list2) {
        if (list1.isEmpty() || list2.isEmpty()) {
            return 0.0;
        }

        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        int intersectionSize = 0;
        for (String element : set1) {
            if (set2.contains(element)) {
                intersectionSize++;
            }
        }

        int unionSize = set1.size() + set2.size() - intersectionSize;

        return (2.0 * intersectionSize) / unionSize;


    }


}
