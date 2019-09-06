package cn.jinelei.smart.archwiki.common.utils;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.jinelei.smart.archwiki.models.BookmarkModel;
import cn.jinelei.smart.archwiki.models.LanguageModel;

public class ModelUtils {

	public static List<BookmarkModel> convertStringToBookmarkModelList(String val) {
		try {
			return Stream.of(val)
				.filter(s -> !s.equals("[]") && s.matches("\\[.*]"))
				.map(s -> s.substring(1, s.length() - 1))
				.flatMap(s -> Stream.of(s.split(",")))
				.filter(s -> !s.equals("\"\""))
				.map(s -> s.substring(1, s.length() - 1))
				.map(s -> s.split("\\^"))
				.filter(s -> s.length >= 2)
				.map(ss -> new BookmarkModel(ss[0], ss[1]))
				.collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public static List<LanguageModel> convertStringToLanguageModelList(String val) {
		try {
			return Stream.of(val)
				.filter(s -> !s.equals("[]") && s.matches("\\[.*]"))
				.map(s -> s.substring(1, s.length() - 1))
				.flatMap(s -> Stream.of(s.split(",")))
				.filter(s -> !s.equals("\"\""))
				.map(s -> s.substring(1, s.length() - 1))
				.map(s -> s.split("\\^"))
				.filter(s -> s.length >= 3)
				.map(ss -> new LanguageModel(ss[0], ss[1], ss[2]))
				.collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public static boolean containsLanguageModel(Collection<LanguageModel> models, LanguageModel languageModel) {
		if (null == languageModel
			|| Strings.isNullOrEmpty(languageModel.getDetailLang())
			|| Strings.isNullOrEmpty(languageModel.getSummaryLang())
			|| null == models || models.size() == 0)
			return false;
		for (LanguageModel item : models) {
			if (item != null && (
				(!Strings.isNullOrEmpty(item.getSummaryLang()) && item.getSummaryLang().equals(languageModel.getSummaryLang()))
					|| (!Strings.isNullOrEmpty(item.getDetailLang()) && item.getDetailLang().equals(languageModel.getDetailLang()))))
				return true;
		}
		return false;
	}

}
