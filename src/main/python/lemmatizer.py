import sys
from pymystem3 import Mystem

mystem = Mystem()

input_sentence = sys.argv[1]
lemmas = mystem.lemmatize(input_sentence)

lemmatized_sentence = ''.join(lemmas).strip()

print(lemmatized_sentence)