# PeyangBanServer

## 概要

Bukkit, Spigot, BungeeCordのプラグインのサーバ機能です。  
BANの管理ができます。 プラグイン自体は[ここ](https://github.com/psac-serve/ban-manager)から入手可能です。

## 通信方式

HTTP1.1を採用しています。

## 使い方

```console
$ java -jar <JARファイル名>
```

で起動できます。

### 初期設定

+ サーバーを起動します。  
  `$ java -jar <ファイル>` で起動してください。
+ `/path/to/PeyangBanServer/config.yml`を編集してください。

#### 編集推奨項目

+ *database*  
  デフォルトでは、SQLiteが使用可能になっています。  
  ですが、MySQLを使用したい場合等もあることでしょう。その場合は、  
  適当にggって設定してください。

#### 必須項目

+ *edit*  
  editは、設定ファイルを生成し、内容を確認したことを判定するために使用されます。  
  **必ず**、`true`にセットしてください。
+ *con.port*  
  デフォルトポート番号は、`810`を使用しているゾ。  
  ３桁番号なので、被る可能性があるゾ。  
  32768等のキリのいい番号を推奨します。

## 認証方式

このサーバでは、トークンによる認証をしています。  
トークンは`token.sig`ファイルに保存されており、データフォルダに作成されます。  
リクエストヘッダーに`token: <Token>`を、**全ての**通信に使用してください。

## 通信ドキュメント

[Swaggerにより自動生成されたドキュメント](https://psac-serve.github.io/ban-server/)を公開いたしました。

## 謝辞

このサーバは、以下のAPI/ライブラリを使用しております。

+ [brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP)
+ [P2P-Develop/PeyangSuperLibrary](https://github.com/P2P-Develop/PeyangSuperLibrary)
+ [xerial/sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)
+ [FasterXML/jackson](https://github.com/FasterXML/jackson)
+ [apache/commons-lang](https://github.com/apache/commons-lang)
+ [mysql/mysql-connector-java](https://github.com/mysql/mysql-connector-j)
