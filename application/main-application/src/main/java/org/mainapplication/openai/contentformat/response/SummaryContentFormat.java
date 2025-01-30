package org.mainapplication.openai.contentformat.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SummaryContentFormat {

	private String summary;

	private String content;

	public static SummaryContentFormat createAlternativeFormat(String summary, String content) {
		return new SummaryContentFormat(summary, content);
	}
}
