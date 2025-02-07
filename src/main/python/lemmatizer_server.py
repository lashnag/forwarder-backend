from flask import Flask, request, jsonify
from pymystem3 import Mystem

app = Flask(__name__)
mystem = Mystem()

@app.route('/lemmatize', methods=['POST'])
def lemmatize():
    data = request.get_json()
    input_sentence = data.get('sentence', '')
    lemmas = mystem.lemmatize(input_sentence)
    lemmatized_sentence = ''.join(lemmas).strip()
    return jsonify({'lemmatized': lemmatized_sentence})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4892)