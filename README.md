- n-gram Overlap Metrics for Content Selection
  - F-score. F-measure
  - Bleu. The Bilingual Evaluation Understudy
  - Rouge. Recall-Oriented Understudy for Gisting Evaluation
  - Meteor. The Metric for Evaluation of Translation with Explicit ORdering
  - Cider. Consensus-based Image Description Evaluation
  - Nist.
  - Gtm. The gtm metric
  - Hlepor. Harmonic mean of enhanced Length Penalty, Precision, n-gram Position difference Penalty, and Recall
  - Ribes. Rank-based Intuitive Bilingual Evaluation Score
  - dice and masi.
- Distance-Based Evaluation Metrics for Content Selection
  - Wer. Word error rate
  - Ter. Translation edit rate
- Vector Similarity-Based Evaluation Metrics
  - Meant 2.0
  - Yisi
  - Word Mover’s Distance
  - Sentence Mover’s Distance (smd)
- n-gram-Based Diversity Metrics
  - Type-Token Ratio (ttr)
  - Self-bleu
- Explicit Semantic Content Match Metrics
  - Pyramid
  - Spice. Semantic propositional image caption evaluation
  - Syntactic Similarity-Based Metrics
  - tesla (Dahlmeier et al., 2011)
- Machine-Learned Evaluation Metrics
  - Enhanced Sequential Inference Model (ESIM)
  - Ruse (Shimanaka et al. (2018)
- Evaluation Models with Human Judgments
  - Adem. Lowe et al. (2017)
  - Huse. Human Unified with Statistical Evaluation. Hashimoto et al. (2019)
  - Bertscore, (Zhang et al., 2020a).
  - Roberta-sts. Kan´e et al. (2019)
  - bleurt (Sellam et al., 2020)
  - comparator evaluator (Zhou & Xu, 2020)
  - Ranking Generated Summaries by Correctness: An Interesting but Challenging Application for Natural Language Inference
  - On Faithfulness and Factuality in Abstractive Summarization
  - Evaluating Semantic Accuracy of Data-to-Text Generation with Natural Language Inference
  - Evaluating Factuality in Generation with Dependency-level Entailment
  - Evaluating the Factual Consistency of Abstractive Text Summarization
  - Apes. Question Answering as an Automatic Evaluation Metric for News Article Summarization
  - Perception Score. Perception Score, A Learned Metric for Open-ended Text Generation Evaluation
- Composite Metric Scores
  - Learning-based Composite Metrics for Improved Caption Evaluation
  - Learning Compact Reward for Image Captioning

# n-gram Overlap Metrics for Content Selection

## F-score. F-measure

- Область применения: machine translation, summarization

## Bleu. The Bilingual Evaluation Understudy 

- Область применения: machine translation, summarization,
- Датасаты с корреляцией: ROCStories, Large Movie Review Conditional, COCO Image Captions, Large Movie Review Unconditional dataset

### Пример использования

```python
from datasets import load_metric
bleu_metric = load_metric('bleu')
# Example reference
ground1 = 'the cat is on the mat'
# Example candidate sentence
candidate = 'the cat the cat on the mat'
ground1 = [candidate.split(' ')]
candidate = [[ground1.split(' ')]]
# Compute the score with bleu.compute
bleu_scores = bleu_metric.compute(predictions=ground1,references=candidate)
print(bleu_scores)
```

## Rouge. Recall-Oriented Understudy for Gisting Evaluation

- Область применения: machine translation, image captioning, question generation
- Подвиды: rouge-{1/2/3/4}, rouge-l

## Meteor. The Metric for Evaluation of Translation with Explicit ORdering

- Область применения: machine translation,  image captioning, question generation, summarization

## Cider. Consensus-based Image Description Evaluation

- Область применения:  image captioning

## Nist.

- Область применения:  machine translation

## Gtm. The gtm metric 

- Область применения:  machine translation

## Hlepor. Harmonic mean of enhanced Length Penalty, Precision, n-gram Position difference Penalty, and Recall 

- Область применения:  machine translation

## Ribes. Rank-based Intuitive Bilingual Evaluation Score

- Область применения:  machine translation

## dice and masi.

- Область применения: генерация описания по картинке с референсом

# Distance-Based Evaluation Metrics for Content Selection

## Wer. Word error rate 

- Область применения: speech recognition systems,
- Ограничения: имеет нижнюю оценку в виде 0 (значит что пример идеален), но не имеет верхнюю оценку что делает трудным оценку

## Ter. Translation edit rate 

- Область применения:  machine translation

# Vector Similarity-Based Evaluation Metrics

## Meant 2.0

- Область применения:  machine translation

## Yisi

- Область применения:  machine translation

## Word Mover’s Distance

- Область применения:  sentence-level task - image caption generation

## Sentence Mover’s Distance (smd)

- Область применения:  оценка документов?

# n-gram-Based Diversity Metrics

## Type-Token Ratio (ttr)

- Область применения:  оценка разнообразности речи

## Self-bleu

- Область применения: оценка разнообразности речи

# Explicit Semantic Content Match Metrics

## Pyramid

- Область применения: document summarization,
- Модификации: PEAK - Pyramid Evaluation via Automated Knowledge Extraction

## Spice. Semantic propositional image caption evaluation

- Область применения:  image captioning

##  Syntactic Similarity-Based Metrics

## tesla (Dahlmeier et al., 2011)

- Область применения:  machine translation, story generation, question generation,  abstractive text summarization

# Machine-Learned Evaluation Metrics

## Enhanced Sequential Inference Model (ESIM)

- Область применения:  machine translation
- Краткое описание: Измеряет похожесть референсов и сгенерированных примеров

# Regression-Based Evaluation

## Ruse (Shimanaka et al. (2018)

Область применения:  machine translation

# Evaluation Models with Human Judgments

## Adem.  Lowe et al. (2017)

- Область применения: chat-bots

## Huse. Human Unified with Statistical Evaluation. Hashimoto et al. (2019)

- Область применения:  dialog and story generation

## Bertscore, (Zhang et al., 2020a).

- Область применения:  semantic evaluation
- Датасаты с корреляцией: ROCStories, Large Movie Review Conditional, COCO Image Captions, Large Movie Review Unconditional dataset

### Пример использования

```python
!pip install bert_score
bertscore_metric = load_metric('bertscore')

bert_scores = bertscore_metric.compute(predictions=[candidate], references=[ground1], lang="en")
print(bert_scores)
# Normally, we use the f1-score attribute
print(bert_scores['f1'])
```

## Roberta-sts. Kan´e et al. (2019) 

- Область применения:  semantic evaluation

## bleurt (Sellam et al., 2020)

- Датасаты с корреляцией: ROCStories, Large Movie Review Conditional, COCO Image Captions, Large Movie Review Unconditional dataset

## comparator evaluator (Zhou & Xu, 2020)

- Область применения:  story generation and open domain dialogue response

## [Ranking Generated Summaries by Correctness: An Interesting but Challenging Application for Natural Language Inference](https://aclanthology.org/P19-1213.pdf)

- Область применения: abstractive summarization

## [On Faithfulness and Factuality in Abstractive Summarization](https://aclanthology.org/2020.acl-main.173.pdf)

- Область применения: abstractive summarization

## [Evaluating Semantic Accuracy of Data-to-Text Generation with Natural Language Inference](https://aclanthology.org/2020.inlg-1.19.pdf)

- Область применения: data-to-text (D2T) generation

## [Evaluating Factuality in Generation with Dependency-level Entailment](https://aclanthology.org/2020.findings-emnlp.322.pdf)

- Область применения:

## [Evaluating the Factual Consistency of Abstractive Text Summarization](https://arxiv.org/abs/1910.12840)

- Область применения: abstractive document summarization

## Apes. [Question Answering as an Automatic Evaluation Metric for News Article Summarization](https://aclanthology.org/N19-1395.pdf)

- Область применения: abstractive document summarization

## Perception Score. [Perception Score, A Learned Metric for Open-ended Text Generation Evaluation](https://arxiv.org/pdf/2008.03082v2.pdf) 

- Область применения: text generation
- Датасаты с корреляцией: ROCStories, Large Movie Review Conditional, COCO Image Captions, Large Movie Review Unconditional dataset

# Composite Metric Scores

## [Learning-based Composite Metrics for Improved Caption Evaluation](https://aclanthology.org/P18-3003.pdf)

- Область применения: image caption

## [Learning Compact Reward for Image Captioning](https://arxiv.org/abs/2003.10925)

- Область применения: image caption
