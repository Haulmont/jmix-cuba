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

import com.haulmont.cuba.core.model.embedded.AddressEmbedded
import com.haulmont.cuba.core.model.embedded.AddressEmbeddedContainer
import com.haulmont.cuba.core.model.sales.Customer
import com.haulmont.cuba.core.model.sales.Order
import com.haulmont.cuba.core.model.sales.OrderLine
import com.haulmont.cuba.core.model.sales.TestCustomerListener
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.*
import io.jmix.core.entity.BaseEntityInternalAccess
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.TransactionalDataManager
import io.jmix.data.impl.EntityListenerManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.inject.Inject


class DataManagerTransactionalTest extends CoreTestSpecification {
    @Inject
    private Persistence persistence
    @Inject
    private TransactionalDataManager txDataManager
    @Inject
    private FetchPlanRepository viewRepository
    @Inject
    private Metadata metadata
    @Inject
    private EntityStates entityStates

    private FetchPlan baseView

    void setup() {
        baseView = viewRepository.getFetchPlan(Customer, '_base')
    }

    void cleanup() {
    }

    def "create and load in one transaction"() {

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager)
        entityListenerManager.addListener(Customer, TestCustomerListener)
        TestCustomerListener.events.clear()

        Customer customer1 = metadata.create(Customer)
        customer1.name = 'Smith'

        Transaction tx = persistence.createTransaction()
        TransactionSynchronizationManager.setCurrentTransactionName("tx1")

        when:

        txDataManager.save(customer1)

//        dataManager.commit(new CommitContext(customer1).setJoinTransaction(true))

        then:

        println(">>>" + TestCustomerListener.events)

        TestCustomerListener.events.size() == 2
        TestCustomerListener.events[0] == 'onAfterInsert: tx1'
        TestCustomerListener.events[1] == 'onBeforeDetach: tx1'

        when:

        TestCustomerListener.events.clear()

        Customer customer = txDataManager.load(Customer).id(customer1.id).one()

//        LoadContext<Customer> loadContext = LoadContext.create(Customer).setId(customer1.id)
//                .setJoinTransaction(true)
//        Customer customer = dataManager.load(loadContext)

        tx.commit()
        tx.end()

        then:

        println(">>>" + TestCustomerListener.events)

        TestCustomerListener.events.size() == 1
        TestCustomerListener.events[0] == 'onBeforeDetach: tx1'

        customer == customer1

        cleanup:

        tx.end()
        TestSupport.deleteRecord(customer1)

        entityListenerManager.removeListener(Customer, TestCustomerListener)
    }

    def "create and then rollback transaction"() {
        Customer customer1 = metadata.create(Customer)
        customer1.name = 'Smith'

        when:

        Transaction tx = persistence.createTransaction()
        try {
            txDataManager.save(customer1)

//            dataManager.commit(new CommitContext(customer1).setJoinTransaction(true))
        } finally {
            tx.end()
        }

        then:

        !txDataManager.load(Customer).id(customer1.id).optional().isPresent()
    }

    def "create new returns detached entities"() {
        Customer customer1 = metadata.create(Customer)
        customer1.name = 'Smith'

        Transaction tx = persistence.createTransaction()

        when:

        Customer customer = txDataManager.save(customer1)

        then:

        !BaseEntityInternalAccess.isManaged(customer)
        BaseEntityInternalAccess.isDetached(customer)
        !BaseEntityInternalAccess.isNew(customer)

        cleanup:

        tx.end()
        TestSupport.deleteRecord(customer1)
    }

    def "update returns detached entities"() {
        Customer customer1 = metadata.create(Customer)
        customer1.name = 'Smith'
        Customer customer2 = txDataManager.save(customer1)

        Transaction tx = persistence.createTransaction()

        when:

        customer2.name = 'Johns'
        Customer customer = txDataManager.save(customer2)

        then:

        customer.name == 'Johns'
        !BaseEntityInternalAccess.isManaged(customer)
        BaseEntityInternalAccess.isDetached(customer)
        !BaseEntityInternalAccess.isNew(customer)

        cleanup:

        tx.end()
        TestSupport.deleteRecord(customer1)
    }

    def "load returns detached entities"() {
        Customer customer1 = new Customer(name: 'Smith')
        Order order1 = new Order(customer: customer1, number: '111')
        def orderLine11 = new OrderLine(order: order1, productName: 'abc')
        def orderLine12 = new OrderLine(order: order1, productName: 'def')
        txDataManager.save(customer1, order1, orderLine11, orderLine12)

        FetchPlan orderView = new FetchPlan(Order)
                .addProperty('number')
                .addProperty('customer', new FetchPlan(Customer).addProperty('name'))
                .addProperty('orderLines', new FetchPlan(OrderLine).addProperty('productName'))

        Transaction tx = persistence.createTransaction()

        when:

        Order order = txDataManager.load(Order).id(order1.id).view(orderView).one()

        then:

        checkObjectGraph(order)

        cleanup:

        tx.end()
        TestSupport.deleteRecord(orderLine11, orderLine12, order1, customer1)
    }

    def "load list returns detached entities"() {
        Customer customer1 = new Customer(name: 'Smith')
        Order order1 = new Order(customer: customer1, number: '111')
        def orderLine11 = new OrderLine(order: order1, productName: 'abc')
        def orderLine12 = new OrderLine(order: order1, productName: 'def')
        txDataManager.save(customer1, order1, orderLine11, orderLine12)

        FetchPlan orderView = new FetchPlan(Order)
                .addProperty('number')
                .addProperty('customer', new FetchPlan(Customer).addProperty('name'))
                .addProperty('orderLines', new FetchPlan(OrderLine).addProperty('productName'))

        Transaction tx = persistence.createTransaction()

        when:

        List<Order> orders = txDataManager.load(Order)
                .query('select e from test$Order e where e.id = :id').parameter('id', order1.id)
                .view(orderView).list()

        then:

        checkObjectGraph(orders[0])

        cleanup:

        tx.end()
        TestSupport.deleteRecord(orderLine11, orderLine12, order1, customer1)
    }

    def "load entity with embedded"() {
        def embedded = new AddressEmbedded(street: 'street1')
        def container = new AddressEmbeddedContainer(name: 'name1', address: embedded)
        txDataManager.save(container)

        FetchPlan view = new FetchPlan(AddressEmbeddedContainer)
                .addProperty('name')
                .addProperty('address', new FetchPlan(AddressEmbedded)
                        .addProperty('street')
                )
        Transaction tx = persistence.createTransaction()

        when:

        def container1 = txDataManager.load(AddressEmbeddedContainer).id(container.id).view(view).one()

        then:

        container1.address.street == 'street1'

        cleanup:

        tx.end()
        TestSupport.deleteRecord(container)
    }

    private void checkObjectGraph(Order order) {
        assert !BaseEntityInternalAccess.isManaged(order)
        assert BaseEntityInternalAccess.isDetached(order)
        assert !BaseEntityInternalAccess.isNew(order)

        assert !BaseEntityInternalAccess.isManaged(order.customer)
        assert BaseEntityInternalAccess.isDetached(order.customer)
        assert !BaseEntityInternalAccess.isNew(order.customer)

        assert !BaseEntityInternalAccess.isManaged(order.orderLines[0])
        assert BaseEntityInternalAccess.isDetached(order.orderLines[0])
        assert !BaseEntityInternalAccess.isNew(order.orderLines[0])

        assert !BaseEntityInternalAccess.isManaged(order.orderLines[1])
        assert BaseEntityInternalAccess.isDetached(order.orderLines[1])
        assert !BaseEntityInternalAccess.isNew(order.orderLines[1])
    }
}