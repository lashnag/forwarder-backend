from flask import Flask, request, jsonify
from pymystem3 import Mystem

app = Flask("Lemmatizer")
mystem = Mystem()

print("Server started")

@app.route('/lemmatize', methods=['POST'])
def lemmatize():
    data = request.get_json()
    input_sentence = data.get('sentence', '')
    lemmas = mystem.lemmatize(input_sentence)
    lemmatized_sentence = ''.join(lemmas).strip()
    return jsonify({'lemmatized': lemmatized_sentence})

app.run(host='0.0.0.0', port=4892)