package com.jule.db.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemCached implements ICache {

	@Override
	public void init(CacheConfig config){

	}

	@Override
	public void setex(String key, int seconds, String value){

	}

	@Override
	public void expire(String key, int seconds){

	}

	@Override
	public String getStr(String key){
		return null;
	}

	@Override
	public <TEntity> void setexMemInfo(String key, int seconds, TEntity info){

	}

	@Override
	public <TEntity> void setMemInfo(String key, String field, TEntity info){

	}

	@Override
	public <TEntity> void setMemInfo(String key, String field, TEntity info, Class<?>... clazzs){
	}

	@Override
	public <TEntity> void lpushMemInfo(String key, TEntity info){
	}

	@Override
	public <TEntity> TEntity brpopMemInfo(String key, int timeout, Class<TEntity> clazz){
		return null;
	}

	@Override
	public <TEntity> TEntity rpopMemInfo(String key, Class<TEntity> clazz){
		return null;
	}

	@Override
	public <TEntity> TEntity getMemInfo(String key, String field, Class<TEntity> clazz){
		return null;
	}

	@Override
	public <TEntity> TEntity getMemInfo(String key, String field, Class<TEntity> clazz,
	        Class<?>... clazzs){
		return null;
	}

	@Override
	public <TEntity> TEntity getMemInfo(String key, Class<TEntity> clazz){
		return null;
	}

	@Override
	public <TEntity> void setMemList(String key, List<TEntity> memInfos, Class<TEntity> clazz,
	        int seconds){

	}

	@Override
	public void blpopMemList(String key, int seconds){

	}

	@Override
	public <TEntity> void setMemMap(String key, Map<String, TEntity> memInfos, Class<TEntity> clazz){

	}

	@Override
	public <TEntity> List<TEntity> getMemList(String key, Class<TEntity> clazz){
		return null;
	}

	@Override
	public <TEntity> Map<String, TEntity> getMemMap(String key, Class<TEntity> clazz){
		return null;
	}

	@Override
	public void removeMemInfo(String key, String field){

	}

	@Override
	public void removeCacheByKey(String key){

	}

	@Override
	public void clearByKey(byte[] key){

	}

	@Override
	public Set<byte[]> getKeys(String pattern){
		return null;
	}

	@Override
	public boolean acquireLock(String key, String param, int expireTime){
		return false;
	}

	@Override
	public void releaseLock(String key){

	}

	@Override
	public void set(String key, String value){

	}

	@Override
	public <TEntity> void putMemList(String key, List<TEntity> memInfos,
			Class<TEntity> clazz, int seconds) {
		
	}

	@Override
	public <TEntity> void putMemMap(String key, Map<String, TEntity> memInfos,
			Class<TEntity> clazz) {
		
	}

	@Override
	public long getListLength(String key) {
		return 0;
	}

	@Override
	public <TEntity> void putLMemList(String key, List<TEntity> memInfos,
			Class<TEntity> clazz, int seconds, int limit) {
		
	}

	@Override
	public void listTrim(String key, int start, int end) {
		
	}

	@Override
	public long incr(String key) {
		
		return 0;
	}

	@Override
	public long incrBy(String key, long integer) {
		
		return 0;
	}

	@Override
	public void zadd(String key, Map<String, Double> scoreMembers) {
		
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		return null;
	}

	@Override
	public Double zscore(String key, String member) {
		return null;
	}

	@Override
	public void zrem(String key, String... members) {
		
	}

	@Override
	public Long zrank(String key, String member) {
		return null;
	}

	@Override
	public void removeSortSetByKey(String key) {
		
	}

	@Override
	public long zcard(String key) {
		return 0;
	}

	@Override
	public long zcount(String key, double min, double max) {
		return 0;
	}

	@Override
	public long decr(String key) {
		return 0;
	}

	@Override
	public long decrBy(String key, long integer) {
		return 0;
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Long zrevrank(String key, String member) {
		return null;
	}

	@Override
	public void removeCacheByKeyList(List<String> keyList) {
		
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		return -1d;
	}
}
