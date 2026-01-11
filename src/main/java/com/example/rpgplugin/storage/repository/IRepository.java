package com.example.rpgplugin.storage.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * リポジトリの基底インターフェース
 * CRUD操作を定義
 *
 * @param <T> エンティティ型
 * @param <ID> ID型
 */
public interface IRepository<T, ID> {

    /**
     * エンティティを保存
     *
     * @param entity 保存するエンティティ
     * @throws SQLException 保存失敗時
     */
    void save(T entity) throws SQLException;

    /**
     * エンティティを非同期保存
     *
     * @param entity 保存するエンティティ
     */
    void saveAsync(T entity);

    /**
     * IDでエンティティを検索
     *
     * @param id 検索するID
     * @return エンティティ（存在しない場合は空）
     * @throws SQLException 検索失敗時
     */
    Optional<T> findById(ID id) throws SQLException;

    /**
     * すべてのエンティティを取得
     *
     * @return エンティティリスト
     * @throws SQLException 取得失敗時
     */
    List<T> findAll() throws SQLException;

    /**
     * エンティティを削除
     *
     * @param id 削除するエンティティのID
     * @throws SQLException 削除失敗時
     */
    void deleteById(ID id) throws SQLException;

    /**
     * エンティティが存在するか確認
     *
     * @param id 確認するID
     * @return 存在する場合true
     * @throws SQLException 確認失敗時
     */
    boolean existsById(ID id) throws SQLException;

    /**
     * エンティティの数を取得
     *
     * @return エンティティ数
     * @throws SQLException 取得失敗時
     */
    long count() throws SQLException;

    /**
     * バッチ更新を実行
     *
     * @param entities 更新するエンティティリスト
     * @throws SQLException 更新失敗時
     */
    void saveAll(List<T> entities) throws SQLException;

    /**
     * エンティティを非同期バッチ保存
     *
     * @param entities 保存するエンティティリスト
     */
    void saveAllAsync(List<T> entities);
}
