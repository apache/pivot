/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.charts.skin;

import java.util.List;

import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.PieDataset;

import pivot.beans.BeanDictionary;
import pivot.charts.ChartView;
import pivot.collections.Dictionary;

/**
 * <p>Implementation of JFreeChart PieDataset.</p>
 *
 * @author gbrown
 */
@SuppressWarnings("unchecked")
public class PieSeriesDataset implements PieDataset {
    private ChartView.CategorySequence categories;
    private Object series;

    private DatasetGroup datasetGroup = null;

    public PieSeriesDataset(ChartView.CategorySequence categories, Object series) {
        if (categories == null) {
            throw new IllegalArgumentException("categories is null.");
        }

        if (series == null) {
            throw new IllegalArgumentException("series is null.");
        }

        this.categories = categories;
        this.series = series;
    }

    public DatasetGroup getGroup() {
        return datasetGroup;
    }

    public void setGroup(DatasetGroup datasetGroup) {
        this.datasetGroup = datasetGroup;
    }

    public int getItemCount() {
        return categories.getLength();
    }

    public int getIndex(Comparable categoryLabel) {
        if (categoryLabel == null) {
            throw new IllegalArgumentException("categoryLabel is null.");
        }

        int index = -1;
        for (int i = 0, n = categories.getLength(); i < n && index == -1; i++) {
            ChartView.Category category = categories.get(i);

            if (categoryLabel.compareTo(category.getLabel()) == 0) {
                index = i;
            }
        }

        return index;
    }

    public Comparable getKey(int categoryIndex) {
        if (categoryIndex < 0
            || categoryIndex > categories.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        return categories.get(categoryIndex).getLabel();
    }

    public List getKeys() {
        java.util.ArrayList columnKeys = new java.util.ArrayList(categories.getLength());
        for (int i = 0, n = categories.getLength(); i < n; i++) {
            columnKeys.add(categories.get(i).getLabel());
        }

        return columnKeys;
    }

    public Number getValue(int categoryIndex) {
        if (categoryIndex < 0
            || categoryIndex > categories.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        ChartView.Category category = categories.get(categoryIndex);
        String categoryKey = category.getKey();

        Dictionary<String, ?> seriesDictionary;
        if (series instanceof Dictionary<?, ?>) {
            seriesDictionary = (Dictionary<String, ?>)series;
        } else {
            seriesDictionary = new BeanDictionary(series);
        }

        Object value = seriesDictionary.get(categoryKey);
        if (value instanceof String) {
            value = Double.parseDouble((String)value);
        }

        return (Number)value;
    }

    public Number getValue(Comparable categoryLabel) {
        return getValue(getIndex(categoryLabel));
    }

    public void addChangeListener(DatasetChangeListener listener) {
        // No-op
    }

    public void removeChangeListener(DatasetChangeListener listener) {
        // No-op
    }
}
