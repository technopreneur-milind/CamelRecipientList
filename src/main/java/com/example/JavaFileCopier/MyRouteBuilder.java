package com.example.JavaFileCopier;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("file:F:/data/inbox?noop=true").choice().when(header("CamelFileName").endsWith(".xml"))
				.to("direct:xmlOrders").when(header("CamelFileName").endsWith(".doc")).to("direct:docOrders")
				.otherwise().multicast().parallelProcessing().to("direct:errorOrders","direct:loggingendpoint");

		from("direct:xmlOrders").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Received XML order: "
						 + exchange.getIn().getHeader("CamelFileName"));
			}
		});
		
		from("direct:docOrders").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Received doc order: "
						 + exchange.getIn().getHeader("CamelFileName"));
				String recipients = "direct:step1";
				if(!exchange.getIn().getHeader("CamelFileName").toString().startsWith("Test"))
					recipients+=",direct:step2";
				exchange.getIn().setHeader("recipients", recipients);
			}
		}).recipientList(header("recipients"));
		
		from("direct:step1").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Executed Step1 of Doc Order "
						 + exchange.getIn().getHeader("CamelFileName"));
			}
		});
		
		from("direct:step2").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Executed Step2 of Doc Order "
						 + exchange.getIn().getHeader("CamelFileName"));
			}
		});

		from("direct:errorOrders").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Received Bad order: "
						 + exchange.getIn().getHeader("CamelFileName"));
			}
		});
		from("direct:loggingendpoint").process(new Processor() {

			public void process(Exchange exchange) throws Exception {
				System.out.println("Logging the Bad order: "
						 + exchange.getIn().getHeader("CamelFileName"));
			}
		});
	}
}
