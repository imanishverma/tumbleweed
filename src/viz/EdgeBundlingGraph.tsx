import React, {useEffect, useRef} from "react";
import * as d3 from "d3";
import "./EdgeBundlingGraph.css";
import {createRoot, GraphData, line} from "./Model";

interface EdgeBundlingGraphProps {
  data: GraphData;
}

const EdgeBundlingGraph: React.FC<EdgeBundlingGraphProps> = ({data}) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const width = 954;
  const radius = width / 2;

  const colorselected = "#000";
  const colorunselected = "#999";
  const colorin = "#00f";
  const colorout = "#f00";
  const colornone = "#ddd";

  function countableTextDependencies(count: number): string {
    if (count === 1) {
      return "1 dependency";
    } else {
      return count + " dependencies";
    }
  }

  function countableTextDependents(count: number): string {
    if (count === 1) {
      return "1 dependent";
    } else {
      return count + " dependents";
    }
  }

  function effort(dependencies: number, dependents: number): number {
    return dependencies + (dependents / 2);
  }

  function instability(dependencies: number, dependents: number): number {
    let i = dependencies / (dependencies + dependents);
    return Math.round((i + Number.EPSILON) * 100) / 100;
  }

  useEffect(() => {
    if (svgRef.current) {
      const svg = d3.select(svgRef.current);
      svg.selectAll("*").remove();

      const root = createRoot(data, radius);

      svg.attr("viewBox", [-width / 2, -width / 2, width, width]);

      const link = svg.append("g")
        .attr("stroke", colornone)
        .attr("fill", "none")
        .selectAll("path")
        .data(root.leaves().flatMap((leaf: any) => leaf.dependencies))
        .join("path")
        .style("mix-blend-mode", "multiply")
        .attr("d", ([i, o]) => line(i.path(o)))
        .each(function (d) {
          d.path = this;
        });

      function overed(event: any, d: any) {
        link.style("mix-blend-mode", null);
        d3.select(event.currentTarget).attr("fill", colorselected);
        d3.selectAll(d.dependents.map((d: any) => d.path)).attr("stroke", colorin).raise();
        // @ts-ignore
        d3.selectAll(d.dependents.map(([d]) => d.text)).attr("fill", colorin);
        d3.selectAll(d.dependencies.map((d: any) => d.path)).attr("stroke", colorout).raise();
        // @ts-ignore
        d3.selectAll(d.dependencies.map(([, d]) => d.text)).attr("fill", colorout);
      }

      function outed(event: any, d: any) {
        link.style("mix-blend-mode", "multiply");
        d3.select(event.currentTarget).attr("fill", colorunselected);
        d3.selectAll(d.dependents.map((d: any) => d.path)).attr("stroke", null);
        // @ts-ignore
        d3.selectAll(d.dependents.map(([d]) => d.text)).attr("fill", colorunselected).attr("font-weight", null);
        d3.selectAll(d.dependencies.map((d: any) => d.path)).attr("stroke", null);
        // @ts-ignore
        d3.selectAll(d.dependencies.map(([, d]) => d.text)).attr("fill", colorunselected).attr("font-weight", null);
      }

      svg.append("g")
        .attr("font-family", "sans-serif")
        .attr("font-size", 10)
        .selectAll("g")
        .data(root.leaves())
        .join("g")
        .attr("transform", d => `rotate(${d.x * 180 / Math.PI - 90}) translate(${d.y},0)`)
        .append("text")
        .attr("fill", colorunselected)
        .attr("dy", "0.31em")
        .attr("x", d => d.x < Math.PI ? 6 : -6)
        .attr("text-anchor", d => d.x < Math.PI ? "start" : "end")
        .attr("transform", d => d.x >= Math.PI ? "rotate(180)" : null)
        .text(d => d.data.id)
        .attr("dy", "0.31em")
        .attr("x", d => d.x < Math.PI ? 6 : -6)
        .attr("text-anchor", d => d.x < Math.PI ? "start" : "end")
        .attr("transform", d => d.x >= Math.PI ? "rotate(180)" : null)
        .text(d => d.data.id)
        .each(function (d: any) {
          d.text = this;
        })
        .on("mouseover", overed)
        .on("mouseout", outed)
        .call(text => text.append("title").text((d: any) => `${d.data.id}
${countableTextDependencies(d.dependencies.length)}
${countableTextDependents(d.dependents.length)}
Effort* = ${effort(d.dependencies.length, d.dependents.length)}, I = ${isNaN(instability(d.dependencies.length, d.dependents.length)) ? 'N/A' : instability(d.dependencies.length, d.dependents.length)}`
        ));
    }
  }, [data]);

  return <svg ref={svgRef} width={width} height={width}/>;
}

export default EdgeBundlingGraph;
