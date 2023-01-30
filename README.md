# SearchEngine
Проект поискового движка.
Веб-интерфейс проекта представляет собой одну веб-страницу с тремя вкладками:
  ● Dashboard. Эта вкладка открывается по умолчанию. На ней отображается общая статистика по всем сайтам, а также детальная статистика и статус 
по каждому из сайтов (статистика, получаемая позапросу /api/statistics). Выводится общее количество сайтов, статус и время индексации отдельных сайтов.
Общее количество страниц и лемм, так же количество страниц и лемм для каждого сайта по отдельности.
  ● Management. На этой вкладке находятся инструменты управления поисковым движком — запуск и остановка полной 
индексации (переиндексации), а также возможность добавить (обновить) отдельную страницу по ссылке. Индексация нового сайта (добавление данных в БД) 
либо полная переиндексация (обновление данных в БД) осуществляется в данном разделе. Обход одной страницы, либо всего сайта происходит благодаря инструменту 
ForkJoinPool, который собирает все ссылки и заходит по нему собирая данные.
  ● Search. Эта страница предназначена для тестирования поискового движка. На ней находится поле поиска, выпадающий 
список с выбором сайта для поиска, а при нажатии на кнопку «Найти» выводятся результаты поиска (по API-запросу /api/search):
Вся информация на вкладки подгружается путём запросов к API нашего приложения. При нажатии кнопок также отправляются запросы.
Выводятся совпадающие слова с данными которые отсортированы по релевантности. Так-же выводится информация с названием сайта, заголовком и сниппетом.
В качестве конфигурационного файла используется файл yaml. Серверная часть проекта состоит из Контроллера, Сервиса, Модели, Репозитории и ДТО объектов.

Используемый стек для Серверной части:
Java 17;
Spring Boot;
Hibernate;
MySql;

После запуска приложении, мы можем зайти на локальный сервер (по умолчанию http://localhost:8080/), далее перейдя на вкладку Managment, мы можем начать индексацию
сайтов. Во вкладке Dashboard мы сможем увидеть статистику в реальном времени (статус, количество проиндексированных страниц и лемм). После индексации всех сайтов, можем
перейти во вкладку Search и набрать поисковые слова. Движок по релевантности выведет страницы со сниппетами и заголовками страниц, кликнув по нужному ресурсу перейдем
на нужную страницу.
