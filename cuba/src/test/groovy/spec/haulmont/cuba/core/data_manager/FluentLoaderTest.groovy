/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spec.haulmont.cuba.core.data_manager

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.model.sales.Customer
import com.haulmont.cuba.core.model.sales.Status
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.*
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.persistence.TemporalType

class FluentLoaderTest extends CoreTestSpecification {
    @Autowired
    private DataManager dataManager
    @Autowired
    private FetchPlanRepository viewRepository
    @Autowired
    private Metadata metadata
    @Autowired
    private TestSupport testSupport

    private FetchPlan baseView
    private Customer customer, customer2
    private UUID customerId, customer2Id

    void setup() {
        baseView = viewRepository.getFetchPlan(Customer, '_base')

        customer = metadata.create(Customer)
        customer.name = 'Smith'
        customer.status = Status.OK
        customerId = customer.id

        customer2 = metadata.create(Customer)
        customer2.name = 'Johns'
        customer2Id = customer2.id

        dataManager.commit(customer, customer2)
    }

    void cleanup() {
        testSupport.deleteRecord(customer, customer2)
    }

    def "usage examples"() {

        expect:

        // load all

        dataManager.load(Customer).list() instanceof List
        dataManager.load(Customer).one() instanceof Customer
        dataManager.load(Customer).optional() instanceof Optional

        dataManager.load(Customer).fetchPlan('_base').list() instanceof List
        dataManager.load(Customer).fetchPlan(baseView).list() instanceof List

        // load by id

        dataManager.load(Customer).id(customerId).one() == customer
        dataManager.load(Customer).id(customerId).optional() == Optional.of(customer)

        dataManager.load(Customer).id(customerId).fetchPlan('_base').one() == customer
        dataManager.load(Customer).id(customerId).fetchPlan(baseView).one() == customer

        // load by query

        dataManager.load(Customer).query('select c from test$Customer c').list() instanceof List
        dataManager.load(Customer).query('select c from test$Customer c').one() instanceof Customer
        dataManager.load(Customer).query('select c from test$Customer c').optional() instanceof Optional

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .list() instanceof List

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .one() == customer

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .cacheable(true)
                .list() instanceof List

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .firstResult(10)
                .maxResults(100)
                .list() instanceof List

        // load by simplified query

        dataManager.load(Customer).query('e.name = ?1', 'Smith').one() == customer

        dataManager.load(Customer).query('e.name = ?1 and e.status = ?2', 'Smith', Status.OK).one() == customer

        dataManager.load(Customer).query('from test$Customer c, test$Order o where o.customer = c').list() instanceof List

        dataManager.load(Customer).query('order by e.name').list() instanceof List

        // load by collection of ids

        dataManager.load(Customer).ids(customerId, customer2Id).list()
                .size() == 2

        dataManager.load(Customer).ids([customerId, customer2Id]).list()
                .size() == 2
    }

    def "test LoadContext when loading all"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.entityMetaClass.name == 'test$Customer'
        loadContext.query.queryString == 'select e from test$Customer e'
        loadContext.softDeletion
        !loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).softDeletion(false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).fetchPlan('_base')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView

        when:

        loader = dataManager.load(Customer).fetchPlan(baseView)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView
    }


    def "test LoadContext when loading by id"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).id(customer.id)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.id == customerId
        loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).id(customer.id).softDeletion(false)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).id(customer.id).fetchPlan('_base')
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView

        when:

        loader = dataManager.load(Customer).id(customer.id).fetchPlan(baseView)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView
    }

    def "test LoadContext when loading by query"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c')
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.queryString == 'select c from test$Customer c'
        loadContext.id == null
        loadContext.softDeletion
        !loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where name = :n')
                .parameter('n', 'Smith')
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.parameters['n'] == 'Smith'

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where createTs >= :c')
                .parameter('c', new Date(), TemporalType.DATE)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.parameters['c'] instanceof TemporalValue

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c').cacheable(true)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c').softDeletion(false)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c')
                .firstResult(10)
                .maxResults(100)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.firstResult == 10
        loadContext.query.maxResults == 100
    }

    def "test LoadContext when loading by collection of ids"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.id == null
        loadContext.ids == [customerId, customer2Id]
        loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).softDeletion(false)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).fetchPlan('_base')
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).fetchPlan(baseView)
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.fetchPlan == baseView
    }

    def "test positional params"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where c.name = ?1 and c.email = ?2', "Joe", "joe@mail.com")
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.queryString == 'select c from test$Customer c where c.name = :_p1 and c.email = :_p2'
        loadContext.query.parameters['_p1'] == 'Joe'
        loadContext.query.parameters['_p2'] == 'joe@mail.com'

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where c.a1=?1 and c.a2=?2 and c.a3=?3 and c.a4=?4 and c.a5=?5 and c.a6=?6 and c.a7=?7 and c.a8=?8 and c.a9=?9 and c.a10=?10 and c.a11=?11',
                "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11")
        loadContext = (LoadContext) FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.queryString == 'select c from test$Customer c where c.a1=:_p1 and c.a2=:_p2 and c.a3=:_p3 and c.a4=:_p4 and c.a5=:_p5 and c.a6=:_p6 and c.a7=:_p7 and c.a8=:_p8 and c.a9=:_p9 and c.a10=:_p10 and c.a11=:_p11'
        loadContext.query.parameters['_p1'] == 'v1'
        loadContext.query.parameters['_p2'] == 'v2'
        loadContext.query.parameters['_p3'] == 'v3'
        loadContext.query.parameters['_p4'] == 'v4'
        loadContext.query.parameters['_p5'] == 'v5'
        loadContext.query.parameters['_p6'] == 'v6'
        loadContext.query.parameters['_p7'] == 'v7'
        loadContext.query.parameters['_p8'] == 'v8'
        loadContext.query.parameters['_p9'] == 'v9'
        loadContext.query.parameters['_p10'] == 'v10'
        loadContext.query.parameters['_p11'] == 'v11'
    }
}
