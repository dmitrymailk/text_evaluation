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

## [Bleu. The Bilingual Evaluation Understudy](https://aclanthology.org/P02-1040.pdf) 

- Область применения: machine translation, summarization,
- Датасаты с корреляцией: ROCStories, Large Movie Review Conditional, COCO Image Captions, Large Movie Review Unconditional dataset

### Описание

BLEU (Bilingual Evaluation Understudy) - алгоритм который сравнивает совпадение оригинального текста и сгенерированного на соответствие по различным n gram.

### Принцип работы

- _R1_: The cat is on the mat.
- _R2_: There is a cat on the mat.
- _C1_: The cat and the dog.

BLEU считает количество слов в сгененированном предложении, которые встречаются во всех референсах, и делит это количество на длину слова. Авторы утверждают что 1-gram отвечают за _adequacy_, а более длинные n-grams за _fluency_.

#### Пример 1. $BLEU_1$

$BLEU_1(C1) = 3/5 = 0.6$

Слова the, cat, the. "The" присутствует в R1 и R2. "cat" присутствует в R1 и R2. слова "and" нет в референсах, не учитываем. "the" присутствует, считаем. "dog" - нет.

По итогу имеем 3/len(C1) = 3/5.

#### Пример 2. $BLEU_1$

С2 = The The The The The.

$BLEU_1(C2) = 5/5 = 1$

Слово "The" встречается 5 раз. Поэтому мы получили высокий результат для плохого перевода. Для этого воспользуемся слудующей формулой.

$Count_{clip} = min(Count,MaxRefCount)$

- Count - количество встречаемости слова (как в примере 1)
- MaxRefCount - максимальное количество таких слов в одном из референсов

Используя это ограничение, мы получаем следующее

$BLEU_1(C2) = 2/5 = 0.4$

Так как в "The cat is on the mat" слово the встречается 2 раза.

#### Пример 3. $BLEU_1$

- _R1_: The cat is on the mat.
- _R2_: There is a cat on the mat.
- _C3_: There is a cat on the mat.
- _C4_: Mat the cat is on a there.

$BLEU_1(C3) = 7/7 = 1$

$BLEU_1(C4) = 7/7 = 1$

Так как мы не учитываем порядок слов, то оба этих предложения получат максимальный скор.

Поэтому мы должны использовать n-gram. Для примера возьмем 2-gram

$BLEU_2(C3) = 6/6 = 1$

$BLEU_2(C4) = 0/6 = 0$

Ни одна биграмма не встречается в референсах в случае с предложением C4.

#### Пример 4. brevity penalty

- C5: of the
- R3: It is a guide to action that ensures that the military will forever heed Party commands.
- R4: It is the guiding principle which guarantees the military forces always being under the command of the Party.
- R5: It is the practical guide for the army always to heed the directions of the party.

"of the" встречается в двух примерах целиком, а это значит что BLUE =1

Чтобы решить эту проблему, авторы предлагают штрафовать предложения, которые короче чем самый ближайший референс.

$BP=e^{(1-r/c)}$

- r - длина референса
- с - длина сгенерированного перевода

$BLEU = BP * exp(\sum_{n=1}^Nw_n*logp_n)$

Авторы предлагают в основном использовать N=4, следовательно $w_n=1/N$.

### Ограничения

- ничего не знает об устройстве языка. все токены для него одинаковы.
- чем больше референсов используется, тем больший BLEU мы получаем. В оригинальном исследовании при использовании 4 референсов и 2, мы получаем 0.3468 и 0.2571 соответсвенно. Поэтому важно учитывать количество референсов при сравнении разных систем.

### Корреляция с человеческими оценками

- корреляция(pearson) с Monolingual Judgments составила 0.99 (оригинальная статья)
- корреляция(pearson) с Bilingual Judgments составила 0.96 (оригинальная статья)
- [данная работа 2018 года](https://aclanthology.org/J18-3002.pdf) утверждает следующее: Корреляция BLEU с человеческими оценками очень зависит от деталей оцениваемых систем, точных используемых корпусных текстов и точного протокола, используемого для человеческих оценок. Если это так, то трудно предсказать, будет ли BLEU хорошо коррелировать с человеческими оценками в новом контексте. Даже в их исследовании корреляция сильно разнилась от работы к работе, а речь идет о machine translation,

### Модификации

#### [sacreBLEU](https://github.com/mjpost/sacrebleu)

Так как токенизация сильно влияет на результат, то мы можем получать разные оценки в зависимости от токенизатора. Также в исходном BLEU присутсвует множество параметров, которые будут изменять конечный результат. sacreBLEU приходит на помощь и приводит в порядок данные недостатки.

### Использование в других задачах

#### Dialogue generation

##### [Multi-domain Wizard-of-Oz (MultiWOZ)](https://github.com/budzianowski/multiwoz)

### Пример использования

- данный пример показывает насколько все зависит от библиотеки, которую мы используем

```python
import evaluate
from nltk.translate.bleu_score import sentence_bleu

candidate_1 = "It is a guide to action which ensures that the military always obeys the commands of the party."

candidate_2 = "It is to insure the troops forever hearing the activity guidebook that party direct."

reference_1 = "It is a guide to action that ensures that the military will forever heed Party commands."
reference_2 = "It is the guiding principle which guarantees the military forces always being under the command of the Party."
reference_3 = "It is the practical guide for the army always to heed the directions of the party."

bleu = evaluate.load("bleu")
sacrebleu = evaluate.load("sacrebleu")

references = [
	[
		reference_1,
		reference_2,
		reference_3
	],
]

candidates = [
	candidate_1,
	candidate_2
]
print("bleu")
for candidate in candidates:
	bleu_score = bleu.compute(
		predictions=[candidate],
		references=references
	)
	print(bleu_score['bleu'])

print("sacrebleu")
for candidate in candidates:
	bleu_score = sacrebleu.compute(
		predictions=[candidate],
		references=references
	)
	print(bleu_score['score'])

print("nltk bleu")
for candidate in candidates:
    candidate = candidate.split()
    reference = [item.split() for item in references[0]]
    bleu_score = sentence_bleu(reference, candidate)
    print(bleu_score)

# bleu
# 0.5401725898595141
# 0.0
# sacrebleu
# 54.017258985951415
# 6.699559159060897
# nltk bleu
# 0.4969770530031034
# 5.7264676266231995e-155
```

### Ссылки

- https://huggingface.co/spaces/evaluate-metric/bleu
- https://medium.com/nlplanet/two-minutes-nlp-learn-the-bleu-metric-by-examples-df015ca73a86
- https://aclanthology.org/P02-1040.pdf

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
