package com.atos.talentsbatch.writer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.classify.ClassifierSupport;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyClassifierCompositeItemWriter<T> implements ItemWriter<T> {
	
	private Classifier<T, ItemWriter<? super T>> classifier = new ClassifierSupport<T, ItemWriter<? super T>>(null);

	/**
	 * @param classifier the classifier to set
	 */
	public void setClassifier(Classifier<T, ItemWriter<? super T>> classifier) {
		Assert.notNull(classifier, "A classifier is required.");
		this.classifier = classifier;
	}

	/**
	 * Delegates to injected {@link ItemWriter} instances according to their
	 * classification by the {@link Classifier}.
	 */
    @Override
	public void write(List<? extends T> items) throws Exception {

		Map<ItemWriter<? super T>, List<T>> map = new LinkedHashMap<ItemWriter<? super T>, List<T>>();

		for (T item : items) {
			try{
				ItemWriter<? super T> key = classifier.classify(item);
				if (!map.containsKey(key)) {
					map.put(key, new ArrayList<T>());
				}
				map.get(key).add(item);
			}catch (IllegalStateException e) {
				log.error(e.getMessage());
			}
			
		}

		for (ItemWriter<? super T> writer : map.keySet()) {
			writer.write(map.get(writer));
		}

	}
}
