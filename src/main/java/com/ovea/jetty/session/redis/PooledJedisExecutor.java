/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.jetty.session.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class PooledJedisExecutor implements JedisExecutor {
    private final Pool<Jedis> jedisPool;

    PooledJedisExecutor(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public <V> V execute(JedisCallback<V> cb) {
        Jedis jedis = jedisPool.getResource();
        try {
            return cb.execute(jedis);
        } catch (JedisConnectionException e) {
            // returnBrokenResource cannot return again.
            // Refer http://commons.apache.org/proper/commons-pool/api-1.6/org/apache/commons/pool/ObjectPool.html
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
            throw e;
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

}
