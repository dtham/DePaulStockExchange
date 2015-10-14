package publishers;


class MessagePublisherSubjectFactory {

  /**
   * Creates an instance of a currentMarketPublisherImpl.
   *
   * @return a CurrentMarketPublisherImpl
   */
  private synchronized static
          CurrentMarketPublisherSpecific createCurrentMarketPublisherImpl() {
    return new MessagePublisherSubjectImpl();
  }

  /**
   * Creates a LastSlaePublisherSubjectImpl.
   *
   * @return a LastSlaePublisherSubjectImpl
   */
  private synchronized static
          LastSalePublisherSpecific createLastSalePublisherSubjectImpl() {
    return new MessagePublisherSubjectImpl();
  }

  /**
   * Creates a TickerPublisherSubjectImpl.
   *
   * @return a TickerPublisherSubjectImpl
   */
  private synchronized static
          TickerPublisherSpecific createTickerPublisherSubjectImpl() {
    return new MessagePublisherSubjectImpl();
  }

  /**
   * Creates a MessagePublisherSpecificSubjectImpl.
   *
   * @return a  MessagePublisherSpecificSubjectImpl
   */
  private synchronized static
          MessagePublisherSpecific createMessagePublisherSpecificSubjectImpl() {
    return new MessagePublisherSubjectImpl();
  }

  /**
   * Creates a CurrentMarketPublisher.
   *
   * @return a CurrentMarketPublisher
   */
  protected synchronized static
          CurrentMarketPublisher createCurrentMarketPublisher() {
    return new CurrentMarketPublisher(createCurrentMarketPublisherImpl());
  }

  /**
   * Creates a LastSalePublisher.
   *
   * @return a LastSalePublisher
   */
  protected synchronized static
          LastSalePublisher createLastSalePublisher() {
    return new LastSalePublisher(createLastSalePublisherSubjectImpl());
  }

  /**
   * Creates a TickerPublisher.
   *
   * @return a TickerPublisher
   */
  protected synchronized static
          TickerPublisher createTickerPublisher() {
    return new TickerPublisher(createTickerPublisherSubjectImpl());
  }

  /**
   * Creates a MessagePublisher.
   *
   * @return a MessagePublisher
   */
  protected synchronized static
          MessagePublisher createMessagePublisher() {
    return new MessagePublisher(createMessagePublisherSpecificSubjectImpl());
  }
}