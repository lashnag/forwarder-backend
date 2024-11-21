Это бэк бота через который происходит настройка подписок.

Для работы локально нужно прописать токен бота из botfather в application-dev.yml и запустить env/development/docker-compose.yml

На хостинге переменные передаются через переменные окружения через родительский docker-compose.

При создании бота задайте ему команды

/create_subscription
/fetch_subscriptions