from fastapi import FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from pymystem3 import Mystem

app = FastAPI()
mystem = Mystem()

class SentenceRequest(BaseModel):
    sentence: str

@app.post("/lemmatize")
def lemmatize(request: SentenceRequest):
    input_sentence = request.sentence
    lemmas = mystem.lemmatize(input_sentence)
    lemmatized_sentence = ''.join(lemmas).strip()
    return JSONResponse(content={'lemmatized': lemmatized_sentence})