Это бэк бота через который происходит настройка подписок.

Для работы локально нужно прописать токен бота из botfather в application-dev.yml и запустить env/development/docker-compose.yml

На хостинге переменные передаются через переменные окружения через родительский docker-compose.

При создании бота задайте ему команды

/create_subscription - подписаться
/fetch_subscriptions - получить / удалить подписки
/changelog - список изменений

Так как кнопки в telegram могут быть с callback не больше 64 байт - то с длинными названиями групп могут возникать проблемы при запросах на получение подписок.
Исправить это можно если переделать структуру БД и работать по id.