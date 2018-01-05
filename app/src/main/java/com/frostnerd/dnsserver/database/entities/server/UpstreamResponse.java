package com.frostnerd.dnsserver.database.entities.server;

import com.frostnerd.utils.database.orm.Entity;
import com.frostnerd.utils.database.orm.annotations.Named;
import com.frostnerd.utils.database.orm.annotations.Table;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
@Table(name = "UpstreamResponse")
public class UpstreamResponse extends Entity{
    @Named(name = "ForQuery")
    private DNSQuery forQuery;
}
